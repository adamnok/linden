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

import linden.util.Experimental

object StandardHtmlElementsB:

  trait Buildable:
    private[linden] def build(content: () => Any)(using context: Context): Any

    final private[linden] def withContent(buildable: Buildable): Buildable =
      new BuildableWrapper(content => build(() => buildable.build(() => content())))

    // Operations

    final def apply()(using context: Context): Any = build(() => ())

    final def apply(content: => Any)(using context: Context): Any = build(() => content)

    @Experimental
    inline final def $(content: => Any)(using context: Context): Any = apply(content)

    @Experimental
    inline final def $(buildable: Buildable): Buildable = withContent(buildable)

  class BuildableWrapper(callback: (() => Any) => Context ?=> Any) extends Buildable:
    override private[linden] def build(content: () => Any)(using context: Context): Any =
      callback(content)

  class BuildBlockElement private[StandardHtmlElementsB](
    htmlName: String,
    creator: Context ?=> String => String => (() => Any) => Unit
  ) extends Buildable:

    private[linden] def build(content: () => Any)(using context: Context): Any =
      creator(htmlName)("")(content)

    def apply(identity: String): Buildable =
      new BuildableWrapper(content => creator(htmlName)(identity)(content))

trait StandardHtmlElementsB {

  protected def element(name: String, content: => Unit, context: Context): Unit

  protected def element(name: String, identity: String, content: => Unit, context: Context): Unit

  private def build = new StandardHtmlElementsB.BuildBlockElement(
    _,
    context ?=> name => id => content => element(name, id, content(), context)
  )

  def br(using context: Context): Unit =
    element("br", {}, context)

  def hr(using context: Context): Unit =
    hr("")

  def hr(identity: String)(using context: Context): Unit =
    element("hr", identity, {}, context)

  def div = build("div")

  def h1 = build("h1")

  def h2 = build("h2")

  def h3 = build("h3")

  def h4 = build("h4")

  def h5 = build("h5")

  def h6 = build("h6")

  def h7 = build("h7")

  def h8 = build("h8")

  def h9 = build("h9")

  def p = build("p")

  def a = build("a")

  def i = build("i")

  def b = build("b")

  def strong = build("strong")

  def span = build("span")

  def ul = build("ul")

  def li = build("li")

  def ol = build("ol")

  def table = build("table")

  def tr = build("tr")

  def td = build("td")

  def th = build("th")

  def thead = build("thead")

  def tbody = build("tbody")

  def aside = build("aside")

  def header = build("header")

  def section = build("section")

  def article = build("article")

  def nav = build("nav")

  def footer = build("footer")

  def summary = build("summary")

  def img = build("img")

  def figure = build("figure")

  def label = build("label")

  def audio = build("audio")
}
