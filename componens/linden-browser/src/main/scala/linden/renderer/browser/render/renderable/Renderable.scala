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

package linden.renderer.browser.render.renderable

import linden.definition.{EventData, InputEventData, SimpleEventData}
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLInputElement, HTMLTextAreaElement}
import org.scalajs.dom.{Event, Node}

trait Renderable:
  def native: Node
  def nativeForChildActions: Node = native

  def addEvent(name: String, event: EventData => Unit): Unit = {
    val getEventData: PartialFunction[(String, Event, Node), EventData] = {
      case ("type", event, element: HTMLInputElement) =>
        InputEventData({ () => element.value }, { () => element.checked }, Some(element), Some(event))

      case ("input", event, element: HTMLInputElement) =>
        InputEventData({ () => element.value }, { () => element.checked }, Some(element), Some(event))

      case ("change", event, element: HTMLInputElement) =>
        InputEventData({ () => element.value }, { () => element.checked }, Some(element), Some(event))

      case ("value", event, element: HTMLInputElement) =>
        InputEventData({ () => element.value }, { () => element.checked }, Some(element), Some(event))

      case ("paste", event, element: HTMLInputElement) =>
        InputEventData({ () => element.value }, { () => element.checked }, Some(element), Some(event))

      case ("click", event, element: HTMLInputElement) =>
        InputEventData({ () => element.value }, { () => element.checked }, Some(element), Some(event))

      case ("type" | "input" | "change" | "value" | "paste", event, element: HTMLTextAreaElement) =>
        InputEventData({ () => element.value }, { () => false }, Some(element), Some(event))

      case (name, event, element) =>
        SimpleEventData(Some(element), Some(event))
    }
    native.addEventListener(
      `type` = name,
      listener = { (it: Event) =>
        event(getEventData((name, it, native)))
      },
      useCapture = true
    )
  }

  infix def +=(child: Node): Unit =
    nativeForChildActions.appendChild(child)

  infix def +=(child: Renderable): Unit =
    this += child.native

  infix def +=(children: List[Renderable]): Unit =
    children.foreach(this += _)

  infix def -=(child: Node): Unit =
    nativeForChildActions.removeChild(child)

  infix def -=(child: Renderable): Unit =
    this -= (child.native)

  infix def -=(children: List[Renderable]): Unit =
    children.foreach(this -= _)

  // deprecated ?
  def addChildren(children: List[Renderable]): Unit =
    children.foreach(this += _)

  def replaceAllChildren(children: List[Renderable]): Unit =
    removeAllChildren()
    this += children

  private def removeAllChildren() =
    while (nativeForChildActions.hasChildNodes) {
      this -= nativeForChildActions.lastChild
    }

  def replaceChild(from: dom.Node, to: dom.Node): Unit =
    nativeForChildActions.replaceChild(from, to)

  def replaceChild(from: Renderable, to: Renderable): Unit =
    replaceChild(from.native, to.native)

  def insertBeforeChild(insertingNode: dom.Node, reference: dom.Node): Unit =
    nativeForChildActions.insertBefore(insertingNode, reference)

  def insertBeforeChild(insertingNode:Renderable, reference: dom.Node): Unit =
    insertBeforeChild(insertingNode.native, reference)

  def insertBeforeChild(insertingNode: Renderable, reference: Renderable): Unit =
    insertBeforeChild(insertingNode.native, reference.native)
