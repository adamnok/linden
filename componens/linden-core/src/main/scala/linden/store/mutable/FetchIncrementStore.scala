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

import linden.store.Incrementer
import linden.store.functional.Real
import linden.store.{ClearContext, Store, ReadStore}


object FetchIncrementStore:
  def apply[I, T](index: I, current: T)(update: I => T)(using incrementer: Incrementer[I]): FetchIncrementStore[T] =
    new FetchIncrementStoreImpl[I, T](index, current)(update)

  def ext[T](store: FetchIncrementStore[T]): FetchIncrementStore[T] &  Real[FetchIncrementStore[T]] =
    Ext[T](store)

  trait Child[T] extends FetchIncrementStore[T] with ReadStore.Child[T]:
    protected override val parent: FetchIncrementStore[T]
    override def reset(): Unit = parent.reset()
    override def next(): Unit = parent.next()

  final private class Ext[T](
     protected override val parent: FetchIncrementStore[T]
  ) extends Child[T] with Real[FetchIncrementStore[T]]

trait FetchIncrementStore[T] extends Store[T, T]:
  def reset(): Unit
  def next(): Unit

class FetchIncrementStoreImpl[I, T](index: I, current: T)(update: I => T)(using incrementer: Incrementer[I])
  extends FetchIncrementStore[T]:

  private val source = Store(index -> current)
  private val pipeline = source.map(_._2)

  override def reset(): Unit =
    source.change(index -> current)

  override def next(): Unit =
    source.change { it =>
      val (index, value) = it
      incrementer.next(index) -> value
    }

  override def value: T = pipeline.value

  override def subscribe(callback: T => Unit)(using context: ClearContext): Unit =
    pipeline.subscribe(callback)

  override def lateSubscribe(callback: T => Unit)(using context: ClearContext): Unit =
    pipeline.lateSubscribe(callback)

