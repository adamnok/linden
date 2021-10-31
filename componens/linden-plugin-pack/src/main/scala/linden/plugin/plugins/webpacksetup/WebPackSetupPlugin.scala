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

import linden.plugin.plugins.webpack.config.{JarCDN, JarDirectory, JarFile}
import sbt.Keys._
import sbt._

import java.nio.file.{Files, Path}

object WebPackSetupPlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {
    val webpackSetupConfig = settingKey[WebPackConfig](
      "This is the place (directory) where the plugin is able to work."
    )
    val webpackSetup = TaskKey[Option[Path]]("webpackSetup", "")
  }

  import autoImport._

  // override def trigger = allRequirements

  override lazy val globalSettings = Seq()

  override lazy val projectSettings = Seq(
    webpackSetupConfig := WebPackConfig().repo("webpack"),
    webpackSetup := {
      val resource = (Compile / resourceDirectory).value.toPath
      val resourceFolder = (Compile / resourceManaged).value.toPath
      streams.value.log.info(s"[webpackSetup] ${webpackSetupConfig.value.items}")
      webpackSetupConfig.value.items
        .map {
          case it: NativeWepPackItem =>
            it -> Some(resource /
              it.value
                .replaceAll("\\.", "/")
                .replaceAll("//", "/")
            )
          case it => it -> None
        }
        .filter {
          case (_, Some(path)) => Files.exists(path)
          case (_, None) => true
        }
        .map {
          case (v: NativeWepPackItem, Some(it)) if Files.isDirectory(it) =>
            v -> JarDirectory(v.value)
          case (v: NativeWepPackItem, Some(it)) if Files.isRegularFile(it) =>
            v -> JarFile(v.value)
          case (CDN(v), None) =>
            v -> JarCDN(v.value)
        }
        .map {
          case (Font(_), it) =>
            linden.plugin.plugins.webpack.config.Config(linden.plugin.plugins.webpack.config.Font(it))
          case (Css(_), it) =>
            linden.plugin.plugins.webpack.config.Config(linden.plugin.plugins.webpack.config.Css(it))
          case (Js(_), it) =>
            linden.plugin.plugins.webpack.config.Config(linden.plugin.plugins.webpack.config.Js(it))
          case (Image(_), it) =>
            linden.plugin.plugins.webpack.config.Config(linden.plugin.plugins.webpack.config.Image(it))
        }
        .fold(linden.plugin.plugins.webpack.config.Config())(_ ++ _)
        .writeTo(resourceFolder / linden.plugin.plugins.webpack.config.Config.fileName)
    },
    (Compile / resourceGenerators) += Def.task {
      streams.value.log.info("running webpack setup")
      webpackSetup.value.map(_.toFile).toSeq
    }
  )
}
