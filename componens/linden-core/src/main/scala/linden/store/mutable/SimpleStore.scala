/*
 * MIT LICENCE
 * 
 * Copyright (c) 2021 Adam Nok [adamnok@protonmail.com]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package linden.store.mutable

import linden.store.*
import linden.store.flow.WrapStore
import linden.store.functional.Real
import linden.util.dsl.*
import scala.collection.mutable.ListBuffer

object SimpleStore:
  def apply[T](current: T): SimpleStore[T, T] =
    new SimpleStoreImpl[T, T](current, x => x)

  def option[T]: SimpleStore[T, Option[T]] =
    withTransform[T, Option[T]](None, Some.apply)

  def withTransform[I, O](current: O, transformer: I => O): SimpleStore[I, O] =
     new SimpleStoreImpl[I, O](current, transformer)

  def ext[I, O](store: SimpleStore[I, O]): SimpleStore[I, O] & Real[SimpleStore[I, O]] =
    new Ext[I, O](store)

  trait Child[I, O] extends SimpleStore[I, O] with ReadStore.Child[O]:
    protected override val parent: SimpleStore[I, O]
    override def change(value: I): Unit = parent.change(value)
    override def change(value: O => I): Unit = parent.change(value)
    override def wrapper[T](forRead: O => T, forWrite: T => I): SimpleStore[T, T] =
      parent.wrapper[T](forRead, forWrite)

  final private class Ext[I, O](
    override protected val parent: SimpleStore[I, O]
  ) extends SimpleStore.Child[I, O] with Real[SimpleStore[I, O]]

trait SimpleStore[I, O] extends Store[I, O]:
  def change(value: I): Unit
  def change(value: O => I): Unit
  def wrapper[T](forRead: O => T, forWrite: T => I): SimpleStore[T, T]

open class SimpleStoreImpl[I, O](
  private var current: O,
  private val transformer: I => O
) extends SimpleStore[I, O]:

  type Observer = O => Unit

  private val subscribed: ListBuffer[Observer] = ListBuffer[Observer]()

  override def value: O = current

  override def change(value: I): Unit =
    val transformedValue = transformer(value)
    if (current != transformedValue)
      current = transformedValue
      // .toList call is important due ConcurrentModificationException
      subscribed.toList foreach (_ apply transformedValue)

  override def change(value: O => I): Unit =
    val _current = transformer(value(current))
    if (current != _current)
      current = _current
      // .toList call is important due ConcurrentModificationException
      subscribed.toList foreach (_ apply _current)

  override def subscribe(callback: Observer)(using context: ClearContext): Unit =
    subscribeNative(callback).let(it => context.addCleared(() => it()))

  def subscribeNative(callback: Observer): ClearStoreObserver =
    subscribed += callback
    callback(current)
    new ClearStoreObserver:
      def apply() = subscribed -= callback

  override def lateSubscribe(callback: Observer)(using context: ClearContext): Unit =
    lateSubscribeNative(callback).let(it => context.addCleared(() => it()))

  def lateSubscribeNative(callback: Observer): ClearStoreObserver =
    subscribed += callback
    new ClearStoreObserver:
      def apply() = subscribed -= callback

  def wrapper[T](forRead: O => T, forWrite: T => I): SimpleStore[T, T] =
    WrapStore[I, O, T, T](this, forRead, forWrite)
