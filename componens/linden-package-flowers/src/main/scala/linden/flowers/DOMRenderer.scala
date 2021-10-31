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

package linden.flowers

import linden.di.DI
import linden.renderer.browser.render.{DOMRenderer as CoreDOMRenderer}
import linden.util.LoggerFactory
import org.scalajs.dom

import scala.language.experimental.macros


object DOMRenderer {

  def apply(containerElementId: String, headComponent: Component): Unit =
    apply(
      containerElementId = containerElementId,
      session = new DI {},
      headComponent = headComponent
    )

  def apply(containerElementId: String, session: DI, headComponent: Component): Unit =
    apply(
      container = Option(dom.document.getElementById("app"))
        .getOrElse(
          throw new IllegalArgumentException(s"Not found HTML element with identity `$containerElementId`!")
        ),
      session = session,
      headComponent = headComponent
    )

  def apply(container: dom.Element, headComponent: Component): Unit =
    apply(
      container = container,
      session = new DI {},
      headComponent = headComponent
    )

  def apply(container: dom.Element, session: DI, headComponent: Component): Unit =
    CoreDOMRenderer[DI](
      container = container,
      session = session,
      headComponent = headComponent
    )

  def apply(loggerFactory: LoggerFactory, container: dom.Element, session: DI, headComponent: Component): Unit =
    CoreDOMRenderer[DI](
      loggerFactory = loggerFactory,
      container = container,
      session = session,
      headComponent = headComponent
    )

  def apply(containerElementId: String, headComponent: DIComponent): Unit =
    apply(
      containerElementId = containerElementId,
      session = new DI {},
      headComponent = headComponent
    )

  def apply(containerElementId: String, session: DI, headComponent: DIComponent): Unit =
    apply(
      container = Option(dom.document.getElementById("app"))
        .getOrElse(
          throw new IllegalArgumentException(s"Not found HTML element with identity `$containerElementId`!")
        ),
      session = session,
      headComponent = headComponent
    )

  def apply(container: dom.Element, headComponent: DIComponent): Unit =
    apply(
      container = container,
      session = new DI {},
      headComponent = headComponent
    )

  def apply(container: dom.Element, session: DI, headComponent: DIComponent): Unit =
    CoreDOMRenderer[DI](
      container = container,
      session = session,
      headComponent = headComponent.create(session)
    )

  def apply(loggerFactory: LoggerFactory, container: dom.Element, session: DI, headComponent: DIComponent): Unit =
    CoreDOMRenderer[DI](
      loggerFactory = loggerFactory,
      container = container,
      session = session,
      headComponent = headComponent.create(session)
    )
}

