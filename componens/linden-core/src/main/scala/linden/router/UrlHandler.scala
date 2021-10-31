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

package linden.router

import linden.util.dsl.*

class UrlHandler(private val url: String):
  private lazy val splittedUrl = locationHashSplit(url)

  private def locationHashSplit(locationHashUrl: String): Seq[String] =
    /*
     * Az url hash részét leszedjük és ha nincs, egy `/` jellel
     * a legelejére helyezünk.
     */
    val locationUrl = locationHashUrl match
      case _ if locationHashUrl.isEmpty =>
        "/"
      case _ if locationHashUrl.startsWith("#/") =>
        locationHashUrl.drop(1)
      case _ if locationHashUrl.startsWith("#") =>
        "/" + locationHashUrl.drop(1)
      case _ if locationHashUrl.startsWith("/") =>
        locationHashUrl
      case _ =>
        s"/$locationHashUrl"
    /*
     * Az url-t szébontjuk a `/` jelek mentén és az üres részeket elhagyjuk.
     *
     * Üres rész: `//`
     */
    locationUrl.split("/").filterNot(_.isEmpty).toSeq

  def isPattern(pattern: String): Boolean =
    hashPattern(pattern).isDefined

  def hashPattern(pattern: String): Option[Map[String, String]] =
    hashPattern(locationHashSplit(pattern), splittedUrl)

  /**
    *
    * @param pattern
    * @param currentUrl
    * @return
    */
  private def hashPattern(pattern: Seq[String], currentUrl: Seq[String]): Option[Map[String, String]] =
    if (pattern.length != currentUrl.length) {
      if (pattern.length > currentUrl.length && currentUrl.lastOption.contains("**")) {
        return hashPattern(pattern.dropRight(1), currentUrl)
          .map(it => it + ("**" -> (it.get("**").map(_ + '/').getOrElse("") + pattern.last)))
      }
      if (pattern.length < currentUrl.length && currentUrl.lastOption.contains("**")) {
        return hashPattern(pattern, currentUrl.dropRight(1))
      }
      return None
    }
    try
      (pattern zip currentUrl)
        .map { case (patternItem, currentUrlItem) =>
          patternItem match {
            case _ if patternItem.headOption.contains(':') =>
              Some(patternItem.tail -> currentUrlItem)
            case "*" =>
              None
            case _ if patternItem == currentUrlItem =>
              None
            case _ =>
              throw IllegalArgumentException()
          }
        }
        .filter(_.isDefined)
        .map(_.get)
        .toMap
        .let(Some(_))
    catch
      case _: IllegalArgumentException => None
