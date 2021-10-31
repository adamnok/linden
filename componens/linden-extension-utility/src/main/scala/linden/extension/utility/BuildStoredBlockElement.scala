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
import linden.store.ReadStore

object BuildStoredBlockElement:
  def apply[RS](identity: String)(creator: (String, Option[ReadStore[RS]], (() => Any)) => Context ?=> Any): BuildStoredBlockElement[RS] =
    new BuildStoredBlockElement(initIdentity = identity, creator = creator)

class BuildStoredBlockElement[RS] private(
  initIdentity: String = "",
  creator: (String, Option[ReadStore[RS]], (() => Any)) => Context ?=> Any
) extends Buildable:

  private[linden] def build(content: () => Any)(using context: Context): Any =
    creator(initIdentity, None, content)

  def apply(identity: String): Buildable =
    new BuildableWrapper(content => creator(s"$initIdentity $identity", None, content))

  def apply(store: ReadStore[RS]): Buildable =
    new BuildableWrapper(content => creator(initIdentity, Some(store), content))

  def apply(store: ReadStore[RS], identity: String): Buildable =
    new BuildableWrapper(content => creator(s"$initIdentity $identity", Some(store), content))
