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

package linden.plugin.plugins.sasspack

import linden.plugin.utils.io.IOFiles
import sbt.Keys.artifact
import sbt.{Attributed, pathToPathOps}

import java.io.File
import java.nio.file._

private[sasspack] class SassPackPluginInternal {
  def copySassFilesFromJar(sourceWorkingPath: Path, allSass: Path)(classpathEntry: Attributed[File]): Unit = {
    classpathEntry.get(artifact.key) match {
      case Some(entryArtifact) =>
        val jarFile = classpathEntry.data
        try {
          val jarFileSystem = FileSystems.newFileSystem(jarFile.toPath, null)
          val path = jarFileSystem.getPath("SassCompilableFile.conf")
          if (Files.exists(path)) {

            val content = Files.readString(path)
            
            val config = content.split("\n")
              .map(_.trim)
              .filterNot(_.isEmpty)
              .filterNot(_ startsWith "#")
              .map(_.split("=").map(_.trim))
              .map { it =>
                it.head -> it.tail.head
              }.toMap
            val configName = config("Name")
            val configPackage = config("Package").replace(".", "/")
            val configEntryPoint = config("EntryPoint")

            val packagePath = jarFileSystem.getPath(configPackage)

            val targetFolder = sourceWorkingPath / configName
            if (Files.notExists(targetFolder)) {
              IOFiles.copyFiles(packagePath, targetFolder)

              val moduleName = s"module.$configName.sass"
              val moduleContent = s"""\n@import "$configName/$configEntryPoint"\n"""
              Files.writeString(sourceWorkingPath / moduleName, moduleContent, StandardOpenOption.CREATE_NEW)

              val allContent = s"""\n@import "$moduleName"\n"""
              Files.writeString(allSass, allContent, StandardOpenOption.APPEND)
            }
          }
        } catch {
          case _: ProviderNotFoundException => /* skip this jar */
        }
      case _ =>
    }
  }
}
