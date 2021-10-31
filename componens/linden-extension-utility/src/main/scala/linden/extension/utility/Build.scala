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

import linden.definition.{Context, StandardHtmlElementsB}
import linden.store.ReadStore

object Build:
  def apply()(creator: (String, () => Any) => Context ?=> Any): BuildBlockElement =
    BuildBlockElement()(creator)

  def apply(identity: String)(creator: (String, () => Any) => Context ?=> Any): BuildBlockElement =
    BuildBlockElement(identity)(creator)

  def brace(identity: String)(element: StandardHtmlElementsB.BuildBlockElement): BuildBlockElement =
    BuildBlockElement.brace(identity)(element)

  def apply(s: Style)(identity: String)(creator: (String, () => Any) => Context ?=> Any): BuildStyledBlockElement[s.type, s.EntityGroup] =
    BuildStyledBlockElement(s)(identity)(creator)

  def brace(s: Style)(identity: String)(element: StandardHtmlElementsB.BuildBlockElement): BuildStyledBlockElement[s.type, s.EntityGroup] =
    BuildStyledBlockElement.brace(s)(identity)(element)

  def store[RS](identity: String)(creator: (String, Option[ReadStore[RS]], (() => Any)) => Context ?=> Any): BuildStoredBlockElement[RS] =
    BuildStoredBlockElement(identity)(creator)

  def store[RS](s: Style)(identity: String)(creator: (String, Option[ReadStore[RS]], () => Any) => Context ?=> Any): BuildStyledStoredBlockElement[s.type, s.EntityGroup, RS] =
    BuildStyledStoredBlockElement(s)(identity)(creator)
