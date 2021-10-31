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
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSName}
import scala.scalajs.js.native
import org.scalajs.dom.{Event, Node}
import org.scalajs.dom.document
import org.scalajs.dom.raw.{Attr, Element}
import org.scalajs.dom.raw.HTMLInputElement

object RenderableShadowHtmlElement:

  private trait AttachShadowOptions extends js.Object:
    var mode: js.UndefOr[String] = js.undefined

  @js.native
  private trait AttachableShadow extends js.Any:
    def attachShadow(options: AttachShadowOptions): Element = js.native

  extension (node: Node)
    private def asShadow(): Element =
      node.asInstanceOf[AttachableShadow].attachShadow(new AttachShadowOptions {
        mode = "open"
      })
      
final class RenderableShadowHtmlElement(name: String) extends RenderableHtmlElement(name):
  import RenderableShadowHtmlElement.asShadow
  override lazy val native: Element = document.createElement(name)
  override lazy val nativeForChildActions: Element = native.asShadow()