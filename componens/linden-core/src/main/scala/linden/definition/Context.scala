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

import linden.store.ClearContext


final class Context extends ClearContext:
  private var finallyStack: List[Element] = List()
  private var stack: List[BlockElement] = List()
  private var cleared: List[() => Unit] = List()

  /**
   * Context tartalmának kiürítése.
   */
  def clear(): Unit =
    cleared.foreach(_.apply())
    cleared = List()
    finallyStack = List()
    stack = List()

  /**
   * Új HTML tag nyitása.
   *
   * @param name A HTML tag típusa.
   */
  def begin(name: String) =
    stack = BlockElement(name, List(), List(), List()) :: stack

  def end() =
    stack match
      case current :: top :: tail =>
        stack = top.copy(children = top.children :+ current) :: tail
      case current :: tail =>
        finallyStack :+= current
        stack = tail
      case _ => {}

  infix def add(element: InlineElement) =
    stack match
      case current :: tail =>
        stack = current.copy(children = current.children :+ element) :: tail
      case Nil =>
        finallyStack :+= element

  infix def add(attribute: Attribute) =
    stack match
      case current :: tail =>
        stack = current.copy(attributes = current.attributes :+ attribute) :: tail
      case _ => {}

  infix def add(event: Event) =
    stack match
      case current :: tail =>
        stack = current.copy(events = current.events :+ event) :: tail
      case _ => {}

  def value = finallyStack ++ stack

  override def addCleared(callback: () => Unit): Unit = cleared = cleared :+ callback
