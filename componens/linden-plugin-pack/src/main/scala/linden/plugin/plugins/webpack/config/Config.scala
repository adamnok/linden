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

package linden.plugin.plugins.webpack.config

import upickle.default.{macroRW, read, write, ReadWriter => RW}

import java.nio.file.{Files, Path}

final case class Config(items: Seq[ReferenceType]) {
  def writeTo(path: Path): Option[Path] = Config.writeToPath(path, this)

  def ++(o: Config) = new Config(items ++ o.items)
}

object Config {
  implicit val rw: RW[Config] = macroRW

  val fileName = "WebPackKey.bytes"

  def parseFromPath(path: Path): Option[Config] =
    if (Files.exists(path)) Some(read[Config](Files.readString(path)))
    //if (Files.exists(path)) Some(readBinary[Config](Files.readAllBytes(path)))
    else None

  def writeToPath(path: Path, config: Config): Option[Path] = {
    if(config.items.isEmpty) {
      None
    } else {
      Files.createDirectories(path.getParent)
      //Files.write(path, writeBinary(config))
      Some(Files.writeString(path, write(config)))
    }
  }

  def apply(): Config = new Config(Seq())

  def apply(v: ReferenceType): Config = new Config(Seq(v))
}

sealed trait JarItem

object JarItem {
  implicit val rw: RW[JarItem] = macroRW
}

final case class JarDirectory(value: String) extends JarItem

object JarDirectory {
  implicit val rw: RW[JarDirectory] = macroRW
}

final case class JarFile(value: String) extends JarItem

object JarFile {
  implicit val rw: RW[JarFile] = macroRW
}

final case class JarCDN(value: String) extends JarItem

object JarCDN {
  implicit val rw: RW[JarCDN] = macroRW
}

sealed trait ReferenceType

object ReferenceType {
  implicit val rw: RW[ReferenceType] = macroRW
}

case class Font(value: JarItem) extends ReferenceType

object Font {
  implicit val rw: RW[Font] = macroRW
}

case class Css(value: JarItem) extends ReferenceType

object Css {
  implicit val rw: RW[Css] = macroRW
}

case class Js(value: JarItem) extends ReferenceType

object Js {
  implicit val rw: RW[Js] = macroRW
}

case class Image(value: JarItem) extends ReferenceType

object Image {
  implicit val rw: RW[Image] = macroRW
}