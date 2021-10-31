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

package linden.flowers

import org.scalajs.dom.{Element, document, window}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Random

@Experimental
object dynamic {
  def awaitElementById(id: String): Future[Element] = {
    val promise = Promise[Element]
    awaitElementByIdWithNative(id) { element =>
      promise.trySuccess(element)
    }
    promise.future
  }

  def awaitElementByIds(ids: Seq[String])(implicit ec: ExecutionContext): Future[Seq[Element]] =
    Future.sequence(ids.map(awaitElementById))

  def awaitElementByIds(id_1: String, id_2: String)(implicit ec: ExecutionContext): Future[(Element, Element)] =
    awaitElementByIds(Seq(id_1, id_2)).map {
      case Seq(a, b) => a -> b
    }

  def awaitElementByIds(id_1: String, id_2: String, id_3: String)(implicit ec: ExecutionContext): Future[(Element, Element, Element)] =
    awaitElementByIds(Seq(id_1, id_2, id_3)).map {
      case Seq(it_1, it_2, it_3) => (it_1, it_2, it_3)
    }

  def awaitElementByIds(id_1: String, id_2: String, id_3: String, id_4: String)(implicit ec: ExecutionContext): Future[(Element, Element, Element, Element)] =
    awaitElementByIds(Seq(id_1, id_2, id_3, id_4)).map {
      case Seq(it_1, it_2, it_3, it_4) => (it_1, it_2, it_3, it_4)
    }

  def awaitElementByIds(id_1: String, id_2: String, id_3: String, id_4: String, id_5: String)(implicit ec: ExecutionContext): Future[(Element, Element, Element, Element, Element)] =
    awaitElementByIds(Seq(id_1, id_2, id_3, id_4, id_5)).map {
      case Seq(it_1, it_2, it_3, it_4, it_5) => (it_1, it_2, it_3, it_4, it_5)
    }

  def awaitElementByIdWithNative(id: String)(callback: Element => Unit): Unit = {
    repeater(5) { () =>
      val element = document.getElementById(id)
      if (element != null) {
        callback(element)
        false
      } else true
    }
  }

  def repeater[L, R](timeout: Int)(callback: () => Boolean): Unit = {
    val repeatable = callback()
    if (repeatable)
      window.setTimeout({ () => repeater[L, R](timeout)(callback) }, timeout)
  }

  def randomIdForDOM(prefix: String): String = prefix + Random.alphanumeric.take(30).mkString("")
}
