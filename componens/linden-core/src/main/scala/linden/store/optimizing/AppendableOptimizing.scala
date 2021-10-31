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

package linden.store.optimizing

import linden.store.ReadStore
import linden.store.flow.OptimizingStore
import linden.store.optimizing.AppendableOptimizing.Action.*
import linden.store.optimizing.AppendableOptimizing.{Action, AppendableBy, AppendableStore}
import linden.util.dsl.*

object AppendableOptimizing {

  type AppendableValue[V] = (Seq[V], Seq[Action[V]])
  type AppendableStore[V] = ReadStore[AppendableValue[V]]

  trait AppendableBy[T] {
    type SequalizeItem
    type Key

    def sequalize(it: T): Seq[SequalizeItem]

    def createKey(it: SequalizeItem): Key
  }

  abstract class SeqAppendableBy[V, K](by: V => K) extends AppendableBy[Seq[V]] {
    override type SequalizeItem = V
    override type Key = K

    override def sequalize(it: Seq[V]): Seq[SequalizeItem] = it

    override def createKey(it: SequalizeItem): K = by(it)
  }

  trait Action[+T]

  object Action {

    case object Clear extends Action[Nothing]

    case class ToTop[+T](values: Seq[T]) extends Action[T]

    case class ToBottom[+T](values: Seq[T]) extends Action[T]

  }

}

trait AppendableOptimizing[T] {

  abstractOptimizing: AbstractOptimizing[T] =>

  def appendable()(using by: AppendableBy[T]): OptimizingStore[Seq[by.SequalizeItem], Seq[Action[by.SequalizeItem]]] = {
    val optimizing = appendable[by.SequalizeItem, by.Key](
      sequalize = { it => by.sequalize(it) },
      by = { it => by.createKey(it) }
    )
    new OptimizingStore(optimizing)
  }

  private def appendable[G, H](sequalize: T => Seq[G], by: G => H): AppendableStore[G] = {
    case class Item(key: H, value: G):
      override def equals(obj: Any): Boolean =
        obj.asInstanceOf[Matchable] match
          case Item(o_key, _) => o_key == key
          case _ => false
    source
      .map(it => sequalize(it).map(it => Item(by(it), it)))
      .zipWithLastValue
      .map { (previousSeq, newSeq) =>
        val actions: Seq[Action[G]] = (previousSeq, newSeq) match {
          case (None, _) => Seq()
          case (Some(Seq()), seq) => Seq(ToBottom(seq.map(_.value)))
          case (Some(_), Seq()) => Seq(Clear)
          case (Some(last), seq) =>
            val lastHead = last.head
            val lastLast = last.last
            val tops = seq.takeWhile(_ != lastHead)
              .map(_.value)
              .let(it => Option.when(it.nonEmpty)(it))
              .map(ToTop(_))
              .toSeq
            val bottoms = seq.takeLastWhile(_ != lastLast)
              .map(_.value)
              .let(it => Option.when(it.nonEmpty)(it))
              .map(ToBottom(_))
              .toSeq
            tops ++ bottoms
        }
        newSeq.map(_.value) -> actions
      }
  }

}
