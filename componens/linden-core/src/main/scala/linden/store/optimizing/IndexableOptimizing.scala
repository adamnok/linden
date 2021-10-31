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
import linden.store.optimizing.IndexableOptimizing.Action
import linden.store.optimizing.IndexableOptimizing.Action.*
import linden.store.optimizing.IndexableOptimizing.{IndexableBy, IndexableStore}

import scala.annotation.tailrec

object IndexableOptimizing {
  type IndexableValue[V] = (Seq[V], Seq[Action[V]])
  type IndexableStore[V] = ReadStore[IndexableValue[V]]

  trait IndexableBy[T] {
    type SequalizeItem
    type Key

    def sequalize(it: T): Seq[SequalizeItem]

    def createKey(it: SequalizeItem): Key
  }

  trait SeqIndexableBy[V, K](by: V => K) extends IndexableBy[Seq[V]] {
    override type SequalizeItem = V
    override type Key = K

    override def sequalize(it: Seq[V]): Seq[SequalizeItem] = it

    override def createKey(it: SequalizeItem): K = by(it)
  }


  trait Action[+T]

  object Action {

    case object Stay extends Action[Nothing]

    case object Clear extends Action[Nothing]

    case object Remove extends Action[Nothing]

    final case class Insert[+T](value: T) extends Action[T]

    final case class Save(key: Int) extends Action[Nothing]

    final case class Load(key: Int) extends Action[Nothing]

  }

}

trait IndexableOptimizing[T] {

  abstractOptimizing: AbstractOptimizing[T] =>

  def indexable()(using by: IndexableBy[T]): OptimizingStore[Seq[by.SequalizeItem], Seq[Action[by.SequalizeItem]]] = {
    val optimizing = indexable[by.SequalizeItem, by.Key](
      sequalize = { it => by.sequalize(it) },
      by = { it => by.createKey(it) }
    )
    new OptimizingStore(optimizing)
  }

  private def indexable[G, H](sequalize: T => Seq[G], by: G => H): IndexableStore[G] = {
    case class Item(key: H, value: G):
      override def equals(obj: Any): Boolean = obj.asInstanceOf[Matchable] match
        case Item(o_key, _) => o_key == key
        case _ => false
    class Store(elements: Map[H, Int] = Map(), counter: Int = 0):
      def get(item: Item) = elements.get(item.key)
      def contains(item: Item) = elements.contains(item.key)
      def + (item: Item): (Int, Store) =
        if (contains(item)) throw IllegalStateException(s"The item is already exist in the store: ${item}")
        counter -> Store(elements + (item.key -> counter), counter + 1)
      def - (item: Item): Store = Store(elements - item.key)
    source
      .map(it => sequalize(it).map(it => Item(by(it), it)))
      .zipWithLastValue
      .map { case (previousSeq, newSeq) =>
        val actions = (previousSeq, newSeq) match {
          case (None, _) => Seq()
          case (Some(_), Seq()) => Seq(Clear)
          case (Some(last), seq) =>
            @tailrec
            def fn(acc: Seq[Action[G]])(a: Seq[Item], b: Seq[Item])(using store: Store): Seq[Action[G]] = {
              (a, b) match {
                case (Seq(), Seq()) =>
                  acc
                case (Seq(), b_0 :: b_xs) =>
                  store.get(b_0) match
                    case None => fn(acc :+ Insert(b_0.value))(Seq(), b_xs)
                    case Some(id) => fn(acc :+ Load(id))(Seq(), b_xs)(using (store - b_0))                
                case (a_xs, Seq()) =>
                  acc ++ a_xs.map(_ => Remove)
                case (a_0 :: a_xs, b_0 :: b_xs) if a_0 == b_0 =>
                  fn(acc :+ Stay)(a_xs, b_xs)
                case (a_0 :: a_xs, b_0 :: b_xs) if !b_xs.contains(a_0) && !a_xs.contains(b_0) =>
                  store.get(b_0) match
                    case None => fn(acc :+ Remove :+ Insert(b_0.value))(a_xs, b_xs)
                    case Some(id) => fn(acc :+ Remove :+ Load(id))(a_xs, b_xs)(using (store - b_0))
                case (a_0 :: a_xs, b_0 :: b_xs) if b_xs.contains(a_0) && !a_xs.contains(b_0) =>
                  fn(acc :+ Insert(b_0.value))(a_0 :: a_xs, b_xs)
                case (a_0 :: a_xs, b_0 :: b_xs) if !b_xs.contains(a_0) && a_xs.contains(b_0) =>
                  fn(acc :+ Remove)(a_xs, b_0 :: b_xs)
                case (a_0 :: a_xs, b_0 :: b_xs) if b_xs.contains(a_0) && a_xs.contains(b_0) =>
                  val (id, n_store) = store + a_0
                  fn(acc :+ Save(id))(a_xs, b_0 :: b_xs)(using n_store)
                case _ =>
                  throw new IllegalStateException("Invalid order in an indexable store!")
              }
            }

            fn(Seq())(last, seq)(using Store())
        }
        newSeq.map(_.value) -> actions
      }
  }
}
