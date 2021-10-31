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

class HtmlEvents(using val context: Context):

  infix def click(callback: EventData => Unit): Unit =
    add("click", callback)

  infix def click(callback: => Unit): Unit =
    add("click", _ => callback)

  infix def doubleClick(callback: EventData => Unit): Unit =
    add("dblclick", callback)

  infix def doubleClick(callback: => Unit): Unit =
    add("dblclick", _ => callback)

  infix  def add(keyValue: (String, EventData => Unit)): Unit =
    add(keyValue._1, keyValue._2)

  def add(eventKey: String)(callback: PartialFunction[EventData, Any]): Unit =
    context.add(
      Event(
        name = eventKey,
        callback = data =>
          if (callback.isDefinedAt(data))
            callback(data)
      )
    )

  private def add(eventKey: String, callback: EventData => Unit): Unit =
    context.add(Event(eventKey, callback))
