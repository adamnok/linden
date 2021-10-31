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

import linden.component.ComponentFactory

/** ****Definiting **** */

sealed trait Attribute {
  val name: String
  val value: String
}

final case class StaticAttribute(
  name: String,
  value: String
) extends Attribute

final case class ReferencedAttribute(
  referenceID: Int,
  name: String,
  value: String
) extends Attribute

sealed trait EventData {
  val onElement: Option[Any]
  val capturedEvent: Option[Any]
}

case class SimpleEventData(onElement: Option[Any], capturedEvent: Option[Any]) extends EventData

final case class InputEventData(value: () => String, checked: () => Boolean, onElement: Option[Any], capturedEvent: Option[Any]) extends EventData

final case class Event(
  name: String,
  callback: EventData => Unit
)

sealed trait Element

sealed trait InlineElement extends Element

case class BlockElement(
  name: String,
  attributes: List[Attribute],
  events: List[Event],
  children: List[Element]
) extends Element

case class ComponentElement[S](
  factory: ComponentFactory[S]
) extends InlineElement

case class TextElement(
  value: String
) extends InlineElement

case class ReferenceElement(
  referenceID: Int
) extends InlineElement

case class ReferenceAppendableElement(
  referenceID: Int
) extends InlineElement

case class ReferenceIndexableElement(
  referenceID: Int
) extends InlineElement

case class SessionInjector[S](
  sessionInjection: S => S,
  children: AbstractHtmlTree
) extends InlineElement

case class CssElement(
  value: String
) extends InlineElement

case class ShadowElement(
  blockElement: BlockElement
) extends InlineElement
