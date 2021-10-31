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

import linden.store.ReadStore
import linden.store.functional.Real
import linden.store.mutable.SimpleStore
import linden.store.functional.Action.implicitConvertForReal
import scala.language.implicitConversions

trait StandardTextareaHtmlElements {

  protected def elementWith(
    name: String,
    identity: String,
    contentInner: (HtmlAttributes, HtmlEvents) => Any,
    contentOut: => Unit
  )(using context: Context): Unit

  def textarea(
    store: ReadStore[String] & Real[SimpleStore[String, String]]
  )(using context: Context): Unit =
    textarea(store: SimpleStore[String, String])

  def textarea(
    identity: String,
    store: ReadStore[String] & Real[SimpleStore[String, String]]
  )(using context: Context): Unit =
    textarea(identity, store: SimpleStore[String, String])

  def textarea(
    store: SimpleStore[String, String]
  )(using context: Context): Unit =
    textarea("", store)

  def textarea(
    identity: String,
    store: SimpleStore[String, String]
  )(using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) =>
        if (value() != "") {
          store.change(value())
        }
    }
    elementWith(
      name = "textarea",
      identity = identity,
      contentInner = { (o, e) =>
        o add "value" -> store
        List("value", "type", "change", "paste", "cut", "input").foreach { eventKey =>
          e.add(eventKey)(update) // TODO - paste jelentése
        }
      },
      contentOut = {}
    )
  }

  def textareaCustom(
    identity: String,
    store: SimpleStore[String, String]
  )(content: => Unit)(using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) =>
        if (value() != "") {
          store.change(value())
        }
    }
    elementWith(
      name = "textarea",
      identity = identity,
      contentInner = { (o, e) =>
        o add "value" -> store
        List("value", "type", "change", "paste", "cut", "input").foreach { eventKey =>
          e.add(eventKey)(update) // TODO - paste jelentése
        }
      },
      contentOut = content
    )
  }
}
