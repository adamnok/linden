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

package linden.extension.utility

import linden.definition.StandardHtmlElementsB.{Buildable, BuildableWrapper}
import linden.definition.{Context, StandardHtmlElementsB}
import linden.extension.utility.BuildStyledBlockElement.StyleEvaluationParameter


object BuildStyledBlockElement {

  trait StyleEvaluationParameter[T] {
    val init: T
    val compose: (T, T) => T
    val extract: T => String
  }

  def apply(
    s: Style
  )(
    identity: String
  )(
    creator: Context ?=> (String, () => Any) => Any
  ): BuildStyledBlockElement[s.type, s.EntityGroup] =
    new BuildStyledBlockElement[s.type, s.EntityGroup](
      initIdentity = identity,
      expectedStyle = s,
      eval = s.evaluationParameter,
      creator = creator
    )

  def brace(
    s: Style
  )(
    identity: String
  )(
    element: StandardHtmlElementsB.BuildBlockElement
  ): BuildStyledBlockElement[s.type, s.EntityGroup] =
    apply(s)(identity) {
      case (id, content) =>
        element(id)(content())
    }
}

class BuildStyledBlockElement[ExpectedStyle, E] private(
  initIdentity: String = "",
  expectedStyle: ExpectedStyle,
  eval: StyleEvaluationParameter[E],
  creator: Context ?=> (String, () => Any) => Any
) extends Buildable:

  def style: ExpectedStyle = expectedStyle

  private[linden] def build(content: () => Any)(using context: Context): Any =
    creator(s"$initIdentity ${eval.extract(eval.init)}", content)

  def apply(identity: String): Buildable =
    new BuildableWrapper(content => creator(s"$initIdentity $identity ${eval.extract(eval.init)}", content))

  def apply(style: E): Buildable =
    new BuildableWrapper(content => creator(s"$initIdentity ${eval.extract(eval.compose(eval.init, style))}", content))

  def apply(identity: String, style: E): Buildable =
    new BuildableWrapper(content => creator(s"$initIdentity $identity ${eval.extract(style)}", content))

  def withStyle(selector: ExpectedStyle => E, selectors: (ExpectedStyle => E)*): Buildable =
    apply((selector +: selectors).map(_ (style)).reduce(eval.compose))
