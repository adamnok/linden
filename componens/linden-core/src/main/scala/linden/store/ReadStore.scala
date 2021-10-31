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

package linden.store

import linden.store.flow.*
import linden.util.dsl.*

object ReadStore:
  trait Child[T] extends ReadStore[T]:
    protected val parent: ReadStore[T]
    override def value: T = parent.value
    override def subscribe(callback: T => Unit)(using context: ClearContext): Unit = parent.subscribe(callback)
    override def lateSubscribe(callback: T => Unit)(using context: ClearContext): Unit = parent.lateSubscribe(callback)

trait ReadStore[T]:
  def value: T

  def subscribe(callback: T => Unit)(using context: ClearContext): Unit

  def lateSubscribe(callback: T => Unit)(using context: ClearContext): Unit

  /**
   *
   */
  def chainingReduction(using context: ClearContext): ReadStore[T] =
    new ChainingReductionStore(this)

  def chainingDynamicReduction: ReadStore[T] =
    new ChainingDynamicReductionStore(this)

  inline def apply[G](transform: T => G): ReadStore[G] =
    map[G](transform)

  infix def init(initializeValue: T): ReadStore[T] =
    new InitStore[T](this, initializeValue)

  /**
   *
   * ```
   * val store = Store(10)
   * val filteredStore = store.filter(_ > 5)
   *
   * ```
   *
   *
   * @param filter
   * @return
   */
  def filter(condition: T => Boolean): ReadStore[T] =
    new FilteredStore[T](this, condition)

  def filterNot(condition: T => Boolean): ReadStore[T] =
    this.filter(condition.andThen(!_))

  def map[G](transform: T => G): ReadStore[G] =
    new MappedStore[T, G](this, transform)

  def flatMap[G](transform: T => Iterable[G]): ReadStore[G] =
    new FlatMappedStore[T, G](this, transform andThen(_.toSeq))

  inline infix def zip[G](store: ReadStore[G]): ReadStore[(T, G)] =
    join[G](store)

  infix def join[G](store: ReadStore[G]): ReadStore[(T, G)] =
    new JoinedStore[T, G](this, store)

  def also(callback: T => Unit): ReadStore[T] =
    map(_.also(callback))

  def zipWithLastValue: ReadStore[(Option[T], T)] =
    var lastValue: Option[T] = None
    val lock = ""
    this
      .map { v =>
        lock.synchronized {
          val result = lastValue -> v
          lastValue = Some(v)
          result
        }
      }

  def zipWithLastValue(init: T): ReadStore[(T, T)] =
    var lastValue: T = init
    val lock = ""
    this
      .map { v =>
        lock.synchronized {
          val result = lastValue -> v
          lastValue = v
          result
        }
      }

  infix def flowControl(comparator: (T, T) => Boolean): ReadStore[T] =
    this
      .zipWithLastValue
      .map {
        case (None, v) =>
          true -> v
        case (Some(last), v) if !comparator(last, v) =>
          true -> v
        case (Some(last), _) =>
          false -> last
      }
      .let(new FlowControlStore[T](_))

  infix def flowControl[G](extractor: T => G): ReadStore[T] =
    this
      .map(v => v -> extractor(v))
      .zipWithLastValue
      .map {
        case (None, (value, _)) =>
          true -> value
        case (Some((_, lastExtracted)), (value, extractedValue)) if lastExtracted != extractedValue =>
          true -> value
        case (Some((lastValue, _)), _) =>
          false -> lastValue
      }
      .let(new FlowControlStore[T](_))

  def renderOptimizing(): RenderOptimizing[T] = new RenderOptimizing(this)
