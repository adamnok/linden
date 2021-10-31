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

import linden.plugin.utils.Log.ImplicitLogger
import linden.plugin.utils.{SubWorkingDirectory, use, useFuture}
import linden.plugin.utils.io.{IOFiles, StreamGobbler}
import sbt.Keys._
import sbt._

import java.nio.file.{Path, Paths, StandardCopyOption, StandardOpenOption, Files => NioFiles}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SassPackPlugin extends AutoPlugin {

  override def requires: Plugins = plugins.JvmPlugin

  private object Working {
    def source = new SubWorkingDirectory("source")

    def link = new SubWorkingDirectory("link")

    def target = new SubWorkingDirectory("target")
  }

  object autoImport {
    val sassPackWorkingFolder = settingKey[File](
      "This is the place (directory) where the plugin is able to work."
    )

    val sassPackMainSourceFolder = settingKey[Path]("")

    val sassPackMainSourceFileName = settingKey[String]("")

    val sassPackTargetMainFile = settingKey[Path]("")

    val sassCollectDependencies =
      TaskKey[Path]("sassCollectDependencies", "Collect sass dependencies from jars.")

    val sassLinking =
      TaskKey[Path]("sassLinking", "Linking the sass files.")

    val sassNativeCompiler =
      TaskKey[(Int, Path)]("sassNativeCompiler", "Compile sass files.")

    val sassCompiler =
      TaskKey[Seq[File]]("sassCompiler", "Compile sass files.")

    val sass =
      TaskKey[Seq[File]]("sass", "Compile sass files.")
  }

  import autoImport._

  // override def trigger = allRequirements

  private lazy val internalProcessing = new SassPackPluginInternal

  override lazy val projectSettings = Seq(

    sassPackWorkingFolder := (Compile / target).value / "sasspack",

    sassPackMainSourceFolder := (Compile / sourceDirectory).value.toPath / "sass",

    sassPackMainSourceFileName := "main.sass",

    sassPackTargetMainFile := (Compile / resourceManaged).value.toPath / "webpack" / "css" / "main.css",

    sassCollectDependencies := {
      val sassPackFolder = (Working.source from sassPackWorkingFolder).value
      val allSass = sassPackFolder / "_all.sass"

      if (NioFiles.notExists(allSass)) NioFiles.createFile(allSass)

      (Compile / dependencyClasspath).value.foreach {
        internalProcessing.copySassFilesFromJar(sassPackFolder, allSass)
      }
      sassPackFolder
    },

    sassLinking := {
      val sourceFolder = sassCollectDependencies.value
      val linkFolder = (Working.link from sassPackWorkingFolder).value
      val mainInFolder = sassPackMainSourceFolder.value

      val mainInLinkFolder = (linkFolder / sassPackMainSourceFileName.value).toAbsolutePath

      IOFiles.copyFiles(sourceFolder, linkFolder)
      IOFiles.copyFiles(mainInFolder, linkFolder, StandardCopyOption.REPLACE_EXISTING)

      if (NioFiles.notExists(mainInLinkFolder))
        NioFiles.writeString(mainInLinkFolder, """@import "_all.sass"""", StandardOpenOption.CREATE_NEW)

      mainInLinkFolder
    },

    sassNativeCompiler := {
      val s: TaskStreams = streams.value

      val mainInLinkFolder = sassLinking.value

      val targetFolder = (Working.target from sassPackWorkingFolder).value

      val mainInTargetFolder = (targetFolder resolve "main.css").toAbsolutePath

      val exitCode = use(NioFiles.newOutputStream(targetFolder / "sass_exec.txt")) { exec =>
        val command = "powershell.exe"
        val sassCommand =
          s"""
            Start-Process
              -NoNewWindow
              -FilePath sass.bat
              -ArgumentList "$mainInLinkFolder $mainInTargetFolder"
          """.trim.split("\n").map(_.trim).mkString(" ")

        exec.write(s"$command $sassCommand".getBytes)
        exec.flush()
        val process = new ProcessBuilder().command(command, sassCommand).start()

        val futureLog = useFuture(
          NioFiles.newOutputStream(targetFolder / "sass_info.txt"),
          NioFiles.newOutputStream(targetFolder / "sass_error.txt")
        )(StreamGobbler.watch("sass compiler", process)(s.log.asScala))
        val exitCode = process.waitFor()
        Await.ready(futureLog, Duration.Inf)

        s.log("sass compiler $ is done with exti code: " + exitCode)

        exitCode
      }

      exitCode -> mainInTargetFolder
    },

    sassCompiler := {
      val (code, mainInTargetFolder) = sassNativeCompiler.value

      val outPath = sassPackTargetMainFile.value.toAbsolutePath

      if (code == 0) {

        NioFiles.createDirectories(outPath.getParent)
        NioFiles.copy(mainInTargetFolder, outPath, StandardCopyOption.REPLACE_EXISTING)

        Seq(outPath.toFile)
      } else {
        Seq()
      }
    },

    sass := {
      sassCompiler.value
    },

    (Compile / resourceGenerators) += Def.task {
      streams.value.log.info("running sass >> css generator")
      sassCompiler.value
    }
  )
}
