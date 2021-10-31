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

package linden.extension.bulma.htmls.elements

import linden.definition.Html
import linden.extension.bulma.htmls.elements.BulmaButtonHtml.BulmaButtonStyle
import linden.extension.bulma.htmls.util.*
import linden.extension.utility.{Build, Style}

object BulmaButtonHtml:

  object ButtonsStyle extends Style:
    def areSmall = create("are-small")

    def areMedium = create("are-medium")

    def areLarge = create("are-large")

    def hasAddons = create("has-addons")

    def isCentered = create("is-centered")

    def isRight = create("is-right")

  object BulmaButtonStyle extends Style with StyleColors with StyleSizes:
    def isText = create("is-text")

    def isGhost = create("is-ghost")

    def isOutlined = create("is-outlined")

    def isInverted = create("is-inverted")

    def isRounded = create("is-rounded")

    def isFullWidth = create("is-fullwidth")

    def isHovered = create("is-hovered")

    def isFocused = create("is-focused")

    def isActive = create("is-active")

    def isLoading = create("is-loading")

    def isStatic = create("is-static")

    def isBlock = create("is-block")

/**
 * https://bulma.io/documentation/elements/button/
 */
trait BulmaButtonHtml:
  this: Html[?] =>

  def buttons =
    Build.brace("buttons")( div)

  def bulmaButton =
    Build.brace(BulmaButtonStyle)("button ")( a)
