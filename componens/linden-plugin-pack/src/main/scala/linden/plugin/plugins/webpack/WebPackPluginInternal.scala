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

package linden.plugin.plugins.webpack

import linden.plugin.plugins.webpack.config._
import linden.plugin.utils.io.IOFiles
import sbt.Keys.{Classpath, artifact}
import sbt.{Attributed, pathToPathOps}

import java.io.File
import java.nio.file._

private[webpack] class WebPackPluginInternal {

  def transformToWebPackFilesFromSeqJar(sourceWorkingPath: Path)(classpathEntry: Seq[Attributed[File]]) = {
    classpathEntry.map(transformToWebPackFilesFromJar(sourceWorkingPath))
  }

  def transformToWebPackFilesFromJar(sourceWorkingPath: Path)(classpathEntry: Attributed[File]) = {
    classpathEntry.get(artifact.key) match {
      case Some(entryArtifact) =>
        val jarFile = classpathEntry.data
        transformToWebPackFilesFromFolder(sourceWorkingPath)(jarFile)
      case _ =>
    }
  }
  def transformToWebPackFilesFromFolder(sourceWorkingPath: Path)(jarFile: File) = {
    try {
      val jarFileSystem = FileSystems.newFileSystem(jarFile.toPath, null)

      val path = jarFileSystem.getPath(Config.fileName)

      // println(s"Checking ${jarFile.toPath.toString}")

      Config.parseFromPath(path).foreach { config =>
        val cdnFonts = sourceWorkingPath / "cdn-fonts.txt"
        val cdnCss = sourceWorkingPath / "cdn-css.txt"
        val cdnJs = sourceWorkingPath / "cdn-js.txt"

        val fonts = sourceWorkingPath / "fonts"
        val css = sourceWorkingPath / "css"
        val js = sourceWorkingPath / "js"
        val images = sourceWorkingPath / "images"

        Files.createDirectories(fonts)
        Files.createDirectories(css)
        Files.createDirectories(js)
        Files.createDirectories(images)

        Seq(cdnFonts, cdnCss, cdnJs)
          .filter(it => Files.notExists(it))
          .foreach(it => Files.createFile(it))

        config.items.map {
          case Font(JarDirectory(v)) =>
            IOFiles.copyFiles(jarFileSystem getPath v, fonts)
          case Font(JarFile(v)) =>
            Files.copy(jarFileSystem getPath v, fonts)
          case Font(JarCDN(v)) =>
            Files.writeString(cdnFonts, s"$v\n", StandardOpenOption.APPEND)

          case Css(JarDirectory(v)) =>
            IOFiles.copyFiles(jarFileSystem getPath v, css)
          case Css(JarFile(v)) =>
            Files.copy(jarFileSystem getPath v, css)
          case Css(JarCDN(v)) =>
            Files.writeString(cdnCss, s"$v\n", StandardOpenOption.APPEND)

          case Js(JarDirectory(v)) =>
            IOFiles.copyFiles(jarFileSystem getPath v, js)
          case Js(JarFile(v)) =>
            Files.copy(jarFileSystem getPath v, js)
          case Js(JarCDN(v)) =>
            Files.writeString(cdnJs, s"$v\n", StandardOpenOption.APPEND)

          case Image(JarDirectory(v)) =>
            IOFiles.copyFiles(jarFileSystem getPath v, images)
          case Image(JarFile(v)) =>
            Files.copy(jarFileSystem getPath v, images)

          case _ => /* invalid config, ignore */
        }
      }
    } catch {
      case _: ProviderNotFoundException => /* skip this jar */
    }
  }
}
