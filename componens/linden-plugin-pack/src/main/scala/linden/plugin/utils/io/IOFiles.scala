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

package linden.plugin.utils.io

import linden.plugin.utils.Log

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters.iterableAsScalaIterableConverter

object IOFiles {

  private def getExtension(path: Path) = {
    val name = path.getFileName.toString
    if (name.contains('.'))
      Some(name.reverse.takeWhile(_ != '.').reverse)
    else None
  }

  def list(path: Path): Iterable[Path] =
    Files.list(path).collect(Collectors.toList[Path]).asScala

  def copyFile(source: Path, targetDir: Path): Path = {
    val toRename = getExtension(source) == getExtension(targetDir)

    if (!toRename) Files.createDirectories(targetDir)

    if (Files.notExists(source) || Files.isDirectory(source))
      return targetDir

    val targetPath =
      if (toRename) targetDir
      else targetDir.resolve(source.getFileName.toString)

    val nonExists = () => Files.notExists(targetPath)
    val tooOld = () => Files.getLastModifiedTime(targetPath).toMillis < Files.getLastModifiedTime(source).toMillis
    if (nonExists() || tooOld())
      Files.copy(source, targetPath, StandardCopyOption.REPLACE_EXISTING)

    targetPath
  }

  def copyFiles(sourceDir: Path, targetDir: Path, options: CopyOption*): Path = {
    Files.createDirectories(targetDir)

    if (Files.notExists(sourceDir))
      return targetDir

    Files.walkFileTree(sourceDir, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {

        val relativeTargetPath = (sourceDir relativize file).normalize().toString
        val targetPath = targetDir resolve relativeTargetPath

        Files.createDirectories(targetPath.getParent)
        if (options.contains(StandardCopyOption.REPLACE_EXISTING)) {
          Files.copy(file, targetPath, options: _*)
        } else {
          val nonExists = () => Files.notExists(targetPath)
          val tooOld = () => Files.getLastModifiedTime(targetPath).toMillis < Files.getLastModifiedTime(file).toMillis
          if (nonExists() || tooOld())
            Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        super.visitFile(file, attrs)
      }
    })
  }
}
