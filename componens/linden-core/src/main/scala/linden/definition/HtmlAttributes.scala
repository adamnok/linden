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

import linden.definition.HtmlAttributes.{BuildAttribute, Builder}
import linden.store.ReadStore


object HtmlAttributes:

  class Builder(b_addAttribute: (String, String) => Unit, b_addActiveAttribute: (String, ReadStore[String]) => Unit):
    def addAttribute(name: String, value: String): Unit =
      b_addAttribute(name, value)

    def addActiveAttribute(name: String, store: ReadStore[String]): Unit =
      b_addActiveAttribute(name, store)

  class BuildAttribute(builder: Builder)(name: String):
    def apply(value: String): Unit =
      builder.addAttribute(name, value)

    def apply[T](store: ReadStore[T])(transform: T => String): Unit =
      apply(store.map(transform))

    def apply(mapped: ReadStore[String]): Unit =
      builder.addActiveAttribute(name, mapped)

    def option[T](store: ReadStore[Option[T]])(transform: T => String): Unit =
      apply(store.map(_.map(transform).getOrElse("")))

    def option(mapped: ReadStore[Option[String]]): Unit =
      apply(mapped.map(_.getOrElse("")))

class HtmlAttributes(using val context: Context, renderContext: RenderContext):

  private def addAttribute(name: String, value: String): Unit =
    context.add(StaticAttribute(name, value.trim))

  private def addActiveAttribute(name: String, store: ReadStore[String]): Unit =
    val (referenceID, clearReference) = renderContext.attributes.createReferenceID()
    context.add(ReferencedAttribute(referenceID, name, ""))
    store.subscribe { value =>
      renderContext.attributes.reRenderRequest(referenceID, StaticAttribute(name, value.trim))
    }
    context.addCleared { () =>
      //clear.apply()
      clearReference.apply()
    }

  private def builder = Builder(addAttribute, addActiveAttribute)

  private def build = BuildAttribute(builder)(_)

  infix def name = build("name")

  infix def clazz = build("class")

  infix def style = build("style")

  infix def src = build("src")

  infix def href = build("href")

  infix def target = build("target")

  infix def placeholder = build("placeholder")

  infix def width = build("width")

  infix def height = build("height")

  infix def alt = build("alt")

  infix def title = build("title")

  infix def add(key: String, value: String): Unit =
    addAttribute(key, value)

  infix def add(keyValue: (String, ReadStore[String])): Unit =
    addActiveAttribute(keyValue._1, keyValue._2)
