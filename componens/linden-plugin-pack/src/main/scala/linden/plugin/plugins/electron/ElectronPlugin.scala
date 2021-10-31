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

package linden.plugin.plugins.electron

import linden.plugin.plugins.electron.model.{MainJs, PackageJson}
import linden.plugin.utils.Log.ImplicitLogger
import linden.plugin.utils.io.IOFiles
import linden.plugin.utils.staticserver.StaticServer
import sbt.Keys.{libraryDependencies, streams, target}
import sbt.{AutoPlugin, File, Plugins, TaskKey, plugins, settingKey, _}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ElectronPlugin extends AutoPlugin {

  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {
    val electronWorkingDirectory = settingKey[File](
      "This is the place (directory) where the plugin is able to work."
    )
    val electronWebPackWorkingDirectory = settingKey[File](
      "This is the place (directory) where the plugin is able to work."
    )
    val electronBuild =
      TaskKey[Unit]("electronBuild", "Electron build")
  }

  import autoImport._

  private val ScalaVersion = "2.12"

  override lazy val projectSettings = Seq(
    libraryDependencies ++= Seq(),

    electronWorkingDirectory := (Compile / target).value / "electron",

    electronWebPackWorkingDirectory := (Compile / target).value / "webpack",

    electronBuild := {
      implicit val log = streams.value.log.asScala
      import scala.concurrent.ExecutionContext.Implicits.global
      val baseDirectory = electronWorkingDirectory.value.toPath
      val webpackDirectory = electronWebPackWorkingDirectory.value.toPath

      val raw = baseDirectory / "raw"

      PackageJson("test-app").render(raw)
      MainJs().render(raw)

      IOFiles.copyFiles(webpackDirectory, raw / "oolie")
    }
  )
}
