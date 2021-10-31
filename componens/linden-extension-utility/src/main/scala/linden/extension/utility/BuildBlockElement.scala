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


object BuildBlockElement:
  def apply()(creator: Context ?=> (String, () => Any) => Any): BuildBlockElement =
    new BuildBlockElement(initIdentity = "", creator = creator)

  def apply(identity: String)(creator: Context ?=> (String, () => Any) => Any): BuildBlockElement =
    new BuildBlockElement(initIdentity = identity, creator = creator)

  def brace(identity: String)(element: StandardHtmlElementsB.BuildBlockElement): BuildBlockElement =
    apply(identity) {
      case (id, content) => //implicit context =>
        element(id)(content())
    }

class BuildBlockElement private(
  initIdentity: String = "",
  creator: Context ?=> (String, () => Any) => Any
) extends Buildable:

  private[linden] def build(content: () => Any)(using context: Context): Any =
    creator(initIdentity, content)

  def apply(identity: String): Buildable =
    new BuildableWrapper(content => creator(s"$initIdentity $identity", content))
