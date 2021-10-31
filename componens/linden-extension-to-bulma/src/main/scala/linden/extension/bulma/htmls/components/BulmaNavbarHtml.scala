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

import linden.definition.{Context, Html}
import linden.extension.bulma.htmls.components.BulmaNavbarHtml.*
import linden.extension.bulma.htmls.util.StyleColors
import linden.extension.utility.{BuildBlockElement, HtmlPlusExtension, BuildStyledBlockElement, BuildStyledStoredBlockElement, Style}


object BulmaNavbarHtml:
  object NavbarStyle extends Style with StyleColors
  object NavbarItemStyle extends Style:
    def hasDropdown = create("has-dropdown")
    def isActive = create("is-active")

trait BulmaNavbarHtml:
  this: Html[?] & HtmlPlusExtension =>

  lazy val navbarStyle: BulmaNavbarHtml.NavbarStyle.type = NavbarStyle

  def navbar =
    BuildStyledBlockElement(NavbarStyle)("navbar") {
      case (id, content) =>
        nav(id) {
          o.add("role", "navigation")
          o.add("aria-label", "main navigation")
          content()
        }
    }

  def navbarBrand =
    BuildBlockElement.brace("navbar-brand")(div)

  def navbarMenu =
    BuildBlockElement.brace("navbar-menu")(div)

  def navbarStart =
    BuildBlockElement.brace("navbar-start")(div)

  def navbarEnd =
    BuildBlockElement.brace("navbar-end")(div)

  def navbarItem =
    BuildStyledStoredBlockElement[Boolean](NavbarItemStyle)("navbar-item") {
      case (id, store, content) =>
        div(id) {
          if store.isDefined then NavbarItemStyle.hasDropdown.use
          store.foreach(_(Option.when(_)(NavbarItemStyle.isActive)).use)
          content()
        }
    }

  def navbarItemLink =
    BuildBlockElement.brace("navbar-item")(a)

  def navbarDropdown =
    BuildBlockElement.brace("navbar-dropdown")(div)

  def navbarLink =
    BuildBlockElement.brace("navbar-link")(a)

  def navbarDivider(using context: Context): Unit =
    hr("navbar-divider")
