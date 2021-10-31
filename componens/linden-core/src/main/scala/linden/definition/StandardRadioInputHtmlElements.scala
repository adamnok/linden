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

import linden.definition.StandardRadioInputHtmlElements.Election
import linden.store.ReadStore
import linden.store.mutable.SimpleStore

object StandardRadioInputHtmlElements {
  case class Election[T](value: T)
}

trait StandardRadioInputHtmlElements {

  protected def elementWith(
    name: String,
    identity: String,
    contentInner: (HtmlAttributes, HtmlEvents) => Any,
    contentOut: => Unit
  )(using context: Context): Unit

  def inputRadio(
    identity: String,
    store: SimpleStore[String, String],
    currentValue: String
  )(content: => Unit)(using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) =>
        if (checked())
          store.change(currentValue)
    }
    elementWith(
      name = "input",
      identity = identity,
      contentInner = { (o, e) =>
        o.add("type", "radio")
        if (store.value == currentValue)
          o add("checked", "checked")
        List("value", "type", "change", "paste", "cut", "input", "click").foreach { eventKey =>
          e.add(eventKey)(update)
        }
      },
      contentOut = content
    )
  }

  def inputOptRadio(
    identity: String,
    store: SimpleStore[String, Option[String]],
    currentValue: String
  )(using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) =>
        if (checked())
          store.change(currentValue)
    }
    elementWith(
      name = "input",
      identity = identity,
      contentInner = { (o, e) =>
        o.add("type", "radio")
        if (store.value.contains(currentValue))
          o add("checked", "checked")
        List("value", "type", "change", "paste", "cut", "input", "click").foreach { eventKey =>
          e.add(eventKey)(update)
        }
      },
      contentOut = {}
    )
  }

  def inputOpt2Radio(
    identity: String,
    store: SimpleStore[Option[String], Option[String]],
    currentValue: String
  )(using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) =>
        if (checked())
          store.change(Some(currentValue))
    }
    elementWith(
      name = "input",
      identity = identity,
      contentInner = { (o, e) =>
        o.add("type", "radio")
        if (store.value.contains(currentValue))
          o add("checked", "checked")
        List("value", "type", "change", "paste", "cut", "input", "click").foreach { eventKey =>
          e.add(eventKey)(update)
        }
      },
      contentOut = {}
    )
  }
}
