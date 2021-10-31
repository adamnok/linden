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

import linden.definition.{Attribute, StaticAttribute}
import linden.util.dsl.*
import org.scalajs.dom.document
import org.scalajs.dom.raw.{Attr, Element, HTMLInputElement}

open class RenderableHtmlElement(name: String) extends Renderable:
  override lazy val native: Element = document.createElement(name)

  extension (attribute: Attribute)
    def toJS: Attr = document.createAttribute(attribute.name)
      .also(_.value = attribute.value)

  def addAttribute(attribute: StaticAttribute): Unit =
    //native.setAttributeNode(attribute.toJS)
    update(attribute)

  def addAttributes(attributes: Set[StaticAttribute]): Unit =
    attributes foreach addAttribute

  def +=(attributes: Set[StaticAttribute]): Unit =
    addAttributes(attributes)


  infix def update(attribute: StaticAttribute): Unit =
    (attribute.name, native) match
      case ("value", element: HTMLInputElement) =>
        element.value = attribute.value
      case ("checkedbylinden", element: HTMLInputElement) if element.checked != (attribute.value == "true") =>
        element.checked = attribute.value == "true"
      case ("focusbylinden", element: HTMLInputElement) if attribute.value == "true" =>
        element.focus()
      case _ =>
    native.setAttributeNode(attribute.toJS)

  infix def update(attributes: Set[StaticAttribute]): Unit =
    attributes
      .filterNot(it => it.value == native.getAttribute(it.name))
      .foreach(update)
