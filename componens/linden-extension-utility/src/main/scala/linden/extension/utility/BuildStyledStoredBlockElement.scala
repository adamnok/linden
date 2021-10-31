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

import linden.definition.Context
import linden.definition.StandardHtmlElementsB.{Buildable, BuildableWrapper}
import linden.extension.utility.BuildStyledBlockElement.StyleEvaluationParameter
import linden.store.ReadStore


object BuildStyledStoredBlockElement:
  def apply[RS](
    s: Style
  )(
    identity: String
  )(
    creator: (String, Option[ReadStore[RS]], () => Any) => Context ?=> Any
  ): BuildStyledStoredBlockElement[s.type, s.EntityGroup, RS] =
    new BuildStyledStoredBlockElement[s.type, s.EntityGroup, RS](
      initIdentity = identity,
      expectedStyle = s,
      eval = s.evaluationParameter,
      creator = creator
    )

class BuildStyledStoredBlockElement[ExpectedStyle, E, RS] private(
  initIdentity: String = "",
  expectedStyle: ExpectedStyle,
  eval: StyleEvaluationParameter[E],
  creator: (String, Option[ReadStore[RS]], () => Any) => Context ?=> Any
) extends Buildable:

  def style: ExpectedStyle = expectedStyle

  private[linden] def build(content: () => Any)(using context: Context): Any =
    creator(s"$initIdentity ${eval.extract(eval.init)}", None, content)

  def apply(store: ReadStore[RS]): Buildable =
    new BuildableWrapper(content =>
      creator(s"$initIdentity ${eval.extract(eval.init)}", Some(store), content)
    )

  def apply(identity: String): Buildable =
    new BuildableWrapper(content =>
      creator(s"$initIdentity $identity ${eval.extract(eval.init)}", None, content)
    )

  def apply(store: ReadStore[RS], identity: String): Buildable =
    new BuildableWrapper(content =>
      creator(s"$initIdentity $identity ${eval.extract(eval.init)}", None, content)
    )

  def apply(style: E): Buildable =
    new BuildableWrapper(content =>
      creator(s"$initIdentity ${eval.extract(eval.compose(eval.init, style))}", None, content)
    )

  def apply(store: ReadStore[RS], style: E): Buildable =
    new BuildableWrapper(content =>
      creator(s"$initIdentity ${eval.extract(eval.compose(eval.init, style))}", Some(store), content)
    )

  def apply(identity: String, style: E): Buildable =
    new BuildableWrapper(content =>
      creator(s"$initIdentity $identity ${eval.extract(style)}", None, content)
    )

  def apply(store: ReadStore[RS], identity: String, style: E): Buildable =
    new BuildableWrapper(content =>
      creator(s"$initIdentity $identity ${eval.extract(style)}", Some(store), content)
    )
