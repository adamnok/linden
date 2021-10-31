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

package linden.renderer.browser.render

import linden.definition.RenderContextAbstraction.RenderRequestType
import linden.definition.{Attribute, ReferencedAttribute, RenderContext, StaticAttribute}
import linden.renderer.browser.render.renderable.RenderableHtmlElement
import linden.util.Logger

trait DOMAttributeRender:

  val renderContext: RenderContext

  val logger: Logger

  private def observeAttribute(attribute: Attribute)(reRender: StaticAttribute => Unit): Unit =
    attribute match
      case _: StaticAttribute =>
      case attribute: ReferencedAttribute =>
        renderContext.attributes.addObserver(attribute.referenceID, reRender)

  private def observeAttributes(attributes: List[Attribute])(reRender: StaticAttribute => Unit): Unit =
    attributes.foreach(observeAttribute(_)(reRender))

  /**
    * Attribute resolving.
    *
    * @param attribute The reconstituted attribute.
    * @return Response of _1 is true, if `attribute` is non modifiable attribute, otherwise false.
    */
  private def resolveAttribute(attribute: Attribute): Option[(Boolean, StaticAttribute)] =
    attribute match
      case attribute: StaticAttribute =>
        Some(true -> attribute)
      case attribute: ReferencedAttribute =>
        val resolvedAttribute = renderContext.attributes(attribute.referenceID)
        if (resolvedAttribute.name != attribute.name)
          throw new IllegalStateException("Request and cached attribute are not equals.")
        Some(false -> resolvedAttribute)

  private def resolveAttributes(attributes: List[Attribute]): Set[StaticAttribute] =
    val (static, dynamic) = attributes.flatMap(resolveAttribute).partition(_._1)
    val realStatic = static.map(_._2)
    val realDynamic = dynamic.map(_._2).groupBy(_.name).map(it => it._2.last)
    (realStatic ++ realDynamic)
      .groupBy(_.name)
      .map(it => StaticAttribute(it._1, it._2.map(_.value).mkString(" ")))
      .toSet

  protected[linden] def attributes(element: RenderableHtmlElement, attributes: List[Attribute]): Unit =
    element += resolveAttributes(attributes)
    observeAttributes(attributes) { _ =>
      element update resolveAttributes(attributes)
    }
