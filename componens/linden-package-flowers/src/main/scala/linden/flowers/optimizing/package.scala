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

package linden.flowers.optimizing

import linden.store.ReadStore
import linden.store.flow.OptimizingStore
import linden.store.optimizing.IndexableOptimizing
import linden.store.optimizing.AppendableOptimizing

object RenderStrategy:
    case object Indexable
    case object Appendable
    inline def indexable() = Indexable
    inline def appendable() = Appendable

extension [T](store: ReadStore[Seq[T]])

  def optimize(a: RenderStrategy.Indexable.type): OptimizingStore[Seq[T], Seq[IndexableOptimizing.Action[T]]] =
    optimize(a, identity[T])

  def optimize[EQ](a: RenderStrategy.Indexable.type, eqCalc: T => EQ): OptimizingStore[Seq[T], Seq[IndexableOptimizing.Action[T]]] =
    import IndexableOptimizing.*
    val by = new IndexableBy[Seq[T]]:
      type SequalizeItem = T
      type Key = EQ
      def sequalize(it: Seq[T]): Seq[SequalizeItem] = it
      def createKey(it: SequalizeItem): Key = eqCalc(it)
    store.renderOptimizing().indexable()(using by)

  def optimize(a: RenderStrategy.Appendable.type): OptimizingStore[Seq[T], Seq[AppendableOptimizing.Action[T]]] =
    optimize(a, identity[T])

  def optimize[EQ](a: RenderStrategy.Appendable.type, eqCalc: T => EQ): OptimizingStore[Seq[T], Seq[AppendableOptimizing.Action[T]]] =
    import AppendableOptimizing.*
    val by = new AppendableBy[Seq[T]]:
      type SequalizeItem = T
      type Key = EQ
      def sequalize(it: Seq[T]): Seq[SequalizeItem] = it
      def createKey(it: SequalizeItem): Key = eqCalc(it)
    store.renderOptimizing().appendable()(using by)