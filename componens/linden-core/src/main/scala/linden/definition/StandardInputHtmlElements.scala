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


import linden.store.mutable.SimpleStore


trait StandardInputHtmlElements {

  protected def elementWith(
    name: String,
    identity: String,
    contentInner: (HtmlAttributes, HtmlEvents) => Any,
    contentOut: => Unit
  )(using context: Context): Unit


  def inputText(identity: String, store: SimpleStore[String, String])(content: => Unit)(using context: Context): Unit = {
    input(identity, "text", store, content)
  }

  def inputTextByForm(identity: String, store: SimpleStore[String, Option[String]])(content: => Unit)(using context: Context): Unit = {
    inputForm(identity, "text", store, content)
  }

  def inputTextByOptForm(identity: String, store: SimpleStore[Option[String], Option[Option[String]]])(content: => Unit)
                      (using context: Context): Unit = {
    inputOptForm(identity, "text", store, content)
  }

  def inputNumber(identity: String, store: SimpleStore[Int, Int])(content: => Unit)(using context: Context): Unit = {
    inputFromNumber(identity, "number", store, content)
  }

  def inputNumberByForm(identity: String, store: SimpleStore[Int, Option[Int]])(content: => Unit)(using context: Context): Unit = {
    inputFormFromNumber(identity, "number", store, content)
  }

  def inputDate(identity: String, store: SimpleStore[String, String])(content: => Unit)(using context: Context): Unit = {
    input(identity, "date", store, content)
  }

  def inputDateByForm(identity: String, store: SimpleStore[String, Option[String]])(content: => Unit)(using context: Context): Unit = {
    inputForm(identity, "date", store, content)
  }

  def inputCheckbox(store: SimpleStore[Boolean, Boolean])(content: => Unit)(using context: Context): Unit = {
    inputCheckbox("", store)(content)
  }

  def inputCheckbox(identity: String, store: SimpleStore[Boolean, Boolean])(content: => Unit)(using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) => store.change(checked())
    }
    elementWith(
      name = "input",
      identity = identity,
      contentInner = { (o, e) =>
        o.add("type", "checkbox")
        o add "checkedbylinden" -> store.map(_.toString)
        e.add("change")(update)
      },
      contentOut = content
    )
  }

  def inputPassword(identity: String, store: SimpleStore[String, String])(content: => Unit)(using context: Context): Unit = {
    input(identity,"password", store, content)
  }

  def inputPasswordByForm(identity: String, store: SimpleStore[String, Option[String]])(content: => Unit)(using context: Context): Unit = {
    inputForm(identity,"password", store, content)
  }

  private def input(identity: String, inputType: String, store: SimpleStore[String, String], content: => Unit)
                   (using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) => store.change(value())
    }
    elementWith(
      name = "input",
      identity = identity,
      contentInner = { (o, e) =>
        o.add("type", inputType)
        o add "value" -> store
        List("value", "type", "change", "paste", "cut", "input").foreach { eventKey =>
          e.add(eventKey)(update)
        }
      },
      contentOut = content
    )
  }

  private def inputForm(identity: String, inputType: String, store: SimpleStore[String, Option[String]], content: => Unit)
                       (using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) => store.change(value())
    }
    elementWith(
      name = "input",
      identity = identity,
      contentInner = { (o, e) =>
        o.add("type", inputType)
        o add "value" -> store(_.getOrElse(""))
        List("value", "type", "change", "paste", "cut", "input").foreach { eventKey =>
          e.add(eventKey)(update)
        }
      },
      contentOut = content
    )
  }

  private def inputOptForm(identity: String, inputType: String,
                           store: SimpleStore[Option[String], Option[Option[String]]], content: => Unit)
                          (using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) => store.change(Some(value()))
    }
    elementWith(
      name = "input",
      identity = identity,
      contentInner = { (o, e) =>
        o.add("type", inputType)
        o add "value" -> store(_.flatten.getOrElse(""))
        List("value", "type", "change", "paste", "cut", "input").foreach { eventKey =>
          e.add(eventKey)(update)
        }
      },
      contentOut = content
    )
  }

  private def inputFromNumber(identity: String, inputType: String, store: SimpleStore[Int, Int], content: => Unit)
                             (using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) =>
        if (value() != "") {
          store.change(value().toInt)
        }
    }
    elementWith(
      name = "input",
      identity = identity,
      contentInner = { (o, e) =>
        o.add("type", inputType)
        o add "value" -> store.map(_.toString)
        List("value", "type", "change", "paste", "cut", "input").foreach { eventKey =>
          e.add(eventKey)(update)
        }
      },
      contentOut = content
    )
  }

  private def inputFormFromNumber(identity: String, inputType: String, store: SimpleStore[Int, Option[Int]], content: => Unit)
                                 (using context: Context): Unit = {
    val update: PartialFunction[EventData, Any] = {
      case InputEventData(value, checked, _, _) =>
        if (value() != "") {
          store.change(value().toInt)
        }
    }
    elementWith(
      name = "input",
      identity = identity,
      contentInner = { (o, e) =>
        o.add("type", inputType)
        o add "value" -> store.map(_.map(_.toString).getOrElse(""))
        List("value", "type", "change", "paste", "cut", "input").foreach { eventKey =>
          e.add(eventKey)(update)
        }
      },
      contentOut = content
    )
  }


}
