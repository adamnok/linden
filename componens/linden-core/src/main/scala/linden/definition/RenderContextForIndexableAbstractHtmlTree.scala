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

package linden.definition

import linden.definition.RenderContextForIndexableAbstractHtmlTree.*


object RenderContextForIndexableAbstractHtmlTree {

  trait Action

  final case class Init(value: Seq[AbstractHtmlTree]) extends Action

  trait LateAction extends Action

  case object Clear extends LateAction

  case object Stay extends LateAction

  case object Remove extends LateAction

  final case class Insert(value: AbstractHtmlTree) extends LateAction

  final case class Save(id: Int) extends LateAction

  final case class Load(id: Int) extends LateAction

  case class Item(index: Any, value: AbstractHtmlTree)

}

class RenderContextForIndexableAbstractHtmlTree extends RenderContextAbstraction[Seq[Action], Seq[AbstractHtmlTree]] {
  override def updateStoredValue(original: Option[Seq[AbstractHtmlTree]], action: Seq[Action]): Seq[AbstractHtmlTree] = {
    (original, action) match {
      case (None, Seq(Init(value))) => value
      case (None, _) => throw new IllegalStateException()
      case (Some(_), Seq(Clear)) => Seq()
      case (Some(old), actions) =>
        val (response, _, _) = actions.foldLeft(Seq.empty[AbstractHtmlTree], old, Map.empty[Int, AbstractHtmlTree]) {
          case ((acc, xs, m), Insert(n)) => (acc :+ n, xs, m)
          case ((acc, it :: xs, m), Stay) => (acc :+ it, xs, m)
          case ((acc, _ :: xs, m), Remove) => (acc, xs, m)
          case ((acc, it :: xs, m), Save(id)) => (acc, xs, m + (id -> it))
          case ((acc, xs, m), Load(id)) => (acc :+ m(id), xs, m - id)
          case _ => throw new IllegalStateException
        }
        response
    }
  }

  def reRenderRequest(referenceID: Int, action: Action): Unit = {
    reRenderRequest(referenceID, Seq(action))
  }
}
