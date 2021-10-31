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

package linden.store.flow

import linden.store.flow.OptimizingStore.{Init, Optimized, Optimizing, Update}
import linden.store.{ClearContext, ReadStore}

object OptimizingStore:

  sealed trait Optimizing[+I, +U]

  final case class Init[I](value: I) extends Optimizing[I, Nothing]

  final case class Update[U](value: U) extends Optimizing[Nothing, U]

  private case class Optimized[I, U](init: Init[I], update: Update[U])

class OptimizingStore[I, U](store: ReadStore[(I, U)]) extends ReadStore[Optimizing[I, U]]:
  private val s = store.map {
    case (i, u) => Optimized(Init(i), Update(u))
  }

  override def value: Optimizing[I, U] = s.value.init

  override def subscribe(callback: Optimizing[I, U] => Unit)(using context: ClearContext): Unit =
    callback(value)
    lateSubscribe(callback)

  override def lateSubscribe(callback: Optimizing[I, U] => Unit)(using context: ClearContext): Unit =
    s.map(_.update).lateSubscribe(callback)
