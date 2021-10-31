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

package linden.extension.bulma.htmls.layout

import linden.definition.Html
import linden.extension.bulma.htmls.layout.BulmaColumnsHtml.{ColumnStyle, ColumnsStyle}
import linden.extension.utility.{Build, Style}

object BulmaColumnsHtml:

  object ColumnsStyle extends Style:
    def isGapless = create("is-gapless")
    def isCentered = create("is-centered")
    def isVCentered = create("is-vcentered")

  object ColumnStyle extends Style:
    def isThreeQuarters = create("is-three-quarters")

    def isTwoThirds = create("is-two-thirds")

    def isHalf = create("is-half")

    def isOneThird = create("is-one-third")

    def isOnequarter = create("is-one-quarter")

    def isFull = create("is-full")

    def isFourFifths = create("is-four-fifths")

    def isThreeFifths = create("is-three-fifths")

    def isTwoFifths = create("is-two-fifths")

    def isOneFifth = create("is-one-fifth")

    def is1 = create("is-1")

    def is2 = create("is-2")

    def is3 = create("is-3")

    def is4 = create("is-4")

    def is5 = create("is-5")

    def is6 = create("is-6")

    def is7 = create("is-7")

    def is8 = create("is-8")

    def is9 = create("is-9")

    def is10 = create("is-10")

    def is11 = create("is-11")

    def is12 = create("is-12")

    def isOffsetThreeQuarters = create("is-offset-three-quarters")

    def isOffsetTwoThirds = create("is-offset-two-thirds")

    def isOffsetHalf = create("is-offset-half")

    def isOffsetOneThird = create("is-offset-one-third")

    def isOffsetOnequarter = create("is-offset-one-quarter")

    def isOffsetFull = create("is-offset-full")

    def isOffsetFourFifths = create("is-offset-four-fifths")

    def isOffsetThreeFifths = create("is-offset-three-fifths")

    def isOffsetTwoFifths = create("is-offset-two-fifths")

    def isOffsetOneFifth = create("is-offset-one-fifth")

    def isOffset1 = create("is-offset-1")

    def isOffset2 = create("is-offset-2")

    def isOffset3 = create("is-offset-3")

    def isOffset4 = create("is-offset-4")

    def isOffset5 = create("is-offset-5")

    def isOffset6 = create("is-offset-6")

    def isOffset7 = create("is-offset-7")

    def isOffset8 = create("is-offset-8")

    def isOffset9 = create("is-offset-9")

    def isOffset10 = create("is-offset-10")

    def isOffset11 = create("is-offset-11")

    def isOffset12 = create("is-offset-12")

trait BulmaColumnsHtml:
  this: Html[?] =>

  def columns =
    Build.brace(ColumnsStyle)("columns")( div)

  def column =
    Build.brace(ColumnStyle)("column")(div)
