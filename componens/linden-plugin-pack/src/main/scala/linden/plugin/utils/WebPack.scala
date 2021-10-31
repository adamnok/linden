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

package linden.plugin.utils

import java.nio.file.Path


object JSOptStrategy {
  def seq(names: Seq[String]): Seq[JSOptStrategy] =
    names.zipWithIndex.map(it => new JSOptStrategy(it._2, it._1))
}

class JSOptStrategy(val order: Int, val name: String) {
  def fileSuffix = s"-$name.js"

  def directoryName = s"js-$name"

  def indexFileName = s"index-$name.html"

  def is(path: Path) = path.getFileName.toString.endsWith(fileSuffix)
}

object WebPack {

  def jsOptStrategies = JSOptStrategy.seq(Seq("opt", "fastopt"))


  def marketDirectory = {
    Seq(
      "js" -> Seq("js", "jss", "javascript", "javascripts", "jscript", "jscripts"),
      "fonts" -> Seq("fonts"),
      "css" -> Seq("css"),
      "images" -> Seq("img", "imgs", "image", "images")
    ).flatMap { case (it, froms) =>
      froms.map(_ -> it)
    }
  }
}
