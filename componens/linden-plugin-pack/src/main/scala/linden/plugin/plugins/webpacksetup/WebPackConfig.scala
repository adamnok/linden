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

package linden.plugin.plugins.webpacksetup


object WebPackConfig {
  def apply() = new WebPackConfig(Seq())
}

class WebPackConfig private(private[webpacksetup] val items: Seq[WebPackItem]) {
  private def add(v: WebPackItem) = new WebPackConfig(items ++ Seq(v))

  def font(value: String) = add(Font(value))

  def css(value: String) = add(Css(value))

  def js(value: String) = add(Js(value))

  def image(value: String) = add(Image(value))

  def fontCDN(value: String) = add(CDN(Font(value)))

  def cssCDN(value: String) = add(CDN(Css(value)))

  def jsCDN(value: String) = add(CDN(Js(value)))

  def repo(value: String): WebPackConfig =
    this
      .font(s"$value/fonts")
      .font(s"$value/font")
      .css(s"$value/css")
      .js(s"$value/js")
      .js(s"$value/jss")
      .js(s"$value/javascript")
      .js(s"$value/javascripts")
      .js(s"$value/jscript")
      .js(s"$value/jscripts")
      .image(s"$value/img")
      .image(s"$value/imgs")
      .image(s"$value/image")
      .image(s"$value/images")
}

sealed trait WebPackItem

sealed trait NativeWepPackItem {
  val value: String
}

sealed trait CDNAble extends NativeWepPackItem

case class Font(value: String) extends WebPackItem with NativeWepPackItem with CDNAble

case class Css(value: String) extends WebPackItem with NativeWepPackItem with CDNAble

case class Js(value: String) extends WebPackItem with NativeWepPackItem with CDNAble

case class Image(value: String) extends WebPackItem with NativeWepPackItem

case class CDN(value: CDNAble) extends WebPackItem