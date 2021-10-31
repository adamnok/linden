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

package linden.extension.bulma.htmls.components

import linden.definition.Html
import linden.extension.bulma.htmls.components.BulmaTabsHtml.{TabsLinkStyle, TabsStyle}
import linden.extension.bulma.htmls.util.StyleSizes
import linden.extension.utility.*


object BulmaTabsHtml {

  object TabsStyle extends Style with StyleSizes {
    def isCentered = create("is-centered")

    def isRight = create("is-right")

    def isBoxed = create("is-boxed")

    def isToggle = create("is-toggle")

    def isToggleRounded = create("is-toggle-rounded")

    def isFullWidth = create("is-fullwidth")
  }

  object TabsLinkStyle extends Style {
    def isActive = create("is-active")
  }

}

trait BulmaTabsHtml {
  this: Html[?] & HtmlPlusExtension =>

  def tabs =
    Build.brace(TabsStyle)("tabs")(div)

  def tabBody =
    Build.brace("")(ul)

  def tabLink =
    Build.store[Boolean](TabsLinkStyle)("") {
      case (id, store, content) =>
        li {
          a(id) {
            store.foreach(_(Option.when(_)(TabsLinkStyle.isActive)).use)
            content()
          }
        }
    }
}
