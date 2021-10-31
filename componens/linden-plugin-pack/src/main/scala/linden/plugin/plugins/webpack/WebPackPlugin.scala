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

import com.typesafe.config.{ConfigException, ConfigFactory}
import linden.plugin.plugins.webpack.model.index.{Index, WebPackExec, WebPackSource}
import linden.plugin.utils.Log.ImplicitLogger
import linden.plugin.utils.{SubWorkingDirectory, WebPack}
import linden.plugin.utils.io.IOFiles
import sbt.Keys._
import sbt.{Def, _}

import java.nio.file.{Path, StandardCopyOption, StandardOpenOption, Files => NioFiles}
import java.util.stream.Collectors
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object WebPackPlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  private object Working {
    def assets = new SubWorkingDirectory("assets")
  }

  object autoImport {
    val webpackWorkingFolder = settingKey[File](
      "This is the place (directory) where the plugin is able to work."
    )
    val webpackAddedSource = settingKey[File]("Scala.js projects attached to the sbt-web project")

    val webpackSource = settingKey[Option[Project]]("Scala.js projects attached to the sbt-web project")

    val webpackSource2 = TaskKey[Option[WebPackSource]]("webpackSource2")

    val webpackCollectDependencies = TaskKey[Unit]("webpackCollectDependencies", "")
    val webpackCurrentProject = TaskKey[Path]("webpackCurrentProject", "")
    val webpackScalaJsProject = TaskKey[Unit]("webpackScalaJStProject", "")
    val webpackGenerateIndex = TaskKey[Path]("webpackGenerateIndex", "")
    val webpack = TaskKey[Unit]("webpack", "")
  }

  import autoImport._

  // override def trigger = allRequirements

  private lazy val internalProcessing = new WebPackPluginInternal

  override lazy val projectSettings = Seq(
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.1"
    ),
    webpackWorkingFolder := {
      (Compile / target).value / "webpack"
    },
    webpackSource := { None },
    webpackSource2 := Def.taskDyn[Option[WebPackSource]] {
      val sourceProject = webpackSource.value
      sourceProject match {
        case Some(sourceProject) =>
          val workingAssetsFolder = webpackWorkingFolder.value / Working.assets.name
          val log = streams.value.log.asScala
          log.info("[webpackScalaJsProject] Use external ScalaJs project")
          Def.task {
            Some(
              WebPackSource(
                workingAssetsFolder = workingAssetsFolder.toPath,
                scalaJsSourceFolder = (sourceProject / Compile / crossTarget).value.toPath,
                webpackBaseSourceFolder = Seq(
                  (sourceProject / Compile / resourceManaged).value.toPath,
                  (sourceProject / Compile / resourceDirectory).value.toPath
                )
              )
            )
          }
        case _ => Def.task(None)
      }
    }.value,
    webpackCollectDependencies := Def.taskDyn[Unit] {
      val sourceProject = webpackSource.value
      sourceProject match {
        case Some(sourceProject) =>
          Def.task {
            val webPackFolder = (Working.assets from webpackWorkingFolder).value
            val current: Classpath = (Compile / dependencyClasspath).value
            val external: Classpath = (sourceProject / Compile / dependencyClasspath).value
            val files = current.toList ++ external.toList
            files.foreach {
              internalProcessing.transformToWebPackFilesFromJar(webPackFolder)
            }
            ()
          }
        case _ => Def.task(())
      }
    }.value,
    webpackCurrentProject := {
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val log = streams.value.log.asScala
      val webPackFolder = (webpackWorkingFolder.value / Working.assets.name).toPath
      WebPackExec.go(
        webPackFolder,
        (Compile / target).value.toPath / s"scala-${scalaVersion.value}",
        Seq(
          (Compile / resourceManaged).value.toPath,
          (Compile / resourceDirectory).value.toPath
        )
      )

      webPackFolder
    },
    webpackScalaJsProject := Def.taskDyn {
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val log = streams.value.log.asScala
      val webpackSourceInput = webpackSource2.value

      webpackSourceInput match {
        case Some(webpackSourceInput) =>
          Def.task {
            log.info("[webpackScalaJsProject] Use external ScalaJs project")
            WebPackExec.go(webpackSourceInput)
            ()
          }
        case _ => Def.task(())
      }
    }.value,
    webpackGenerateIndex := {
      val sassPackFolder = webpackCurrentProject.value
      val indexPath = webpackWorkingFolder.value.toPath / "index.html"
      val indexConfig = (Compile / resourceDirectory).value.toPath / "webpack" / "IndexConfig.conf"

      val config =
        if (NioFiles.exists(indexConfig)) ConfigFactory.parseFile(indexConfig.toFile)
        else ConfigFactory.empty()

      val index = Index(config)

      def listAssetsReference(subFolder: String, extension: String) =
        if (NioFiles.exists(sassPackFolder / subFolder))
          NioFiles.list(sassPackFolder / subFolder).collect(Collectors.toList[Path]).asScala
            .filter(NioFiles.isRegularFile(_))
            .filter(_.getFileName.toString.endsWith(s".$extension"))
            .map(_.getFileName.toString)
            .map(it => s"assets/$subFolder/$it")
        else Seq()

      def listCDNReference(path: Path) =
        if (NioFiles.exists(path))
          NioFiles.lines(path).collect(Collectors.toList[String]).asScala
        else Seq()

      def listConfigCDNReference(extension: String): Seq[String] = {
        try {
          config.getStringList("cdn").asScala.filter(_.endsWith(s".$extension"))
        } catch {
          case _: ConfigException.Missing => Seq()
        }
      }

      def createAssetsReference(subFolder: String, extension: String)(ref: String => String): String =
        listAssetsReference(subFolder, extension)
          .map(ref)
          .mkString("\n")

      val cssU =
        Seq(
          listCDNReference(sassPackFolder / "cdn-css.txt"),
          listConfigCDNReference("css"),
          listAssetsReference("css", "css")
        ).flatten.map(it => s"""<link rel="stylesheet" href="$it"/>""").mkString("\n")
      val jsU =
        Seq(
          listCDNReference(sassPackFolder / "cdn-js.txt"),
          listConfigCDNReference("js"),
          listAssetsReference("js", "js")
        ).flatten.map(it => s"""<script src="$it"></script>""").mkString("\n")
      val fontsRefs = createAssetsReference("fonts", "ttf") { it =>
        s"""
           |@font-face {
           |font-family: "${it.split("/").last.reverse.dropWhile(_ != '.').tail.reverse}";
           |src: url($it) format("truetype");
           |}""".stripMargin
      }
      val fontsU =
        Seq(
          listCDNReference(sassPackFolder / "cdn-fonts.txt"),
          listConfigCDNReference("fonts")
        ).flatten.map(it => s"""@import url('$it');""").mkString("\n")

      val resolved = WebPack.jsOptStrategies.map { jsOptStrategy =>
        val jsIts = listAssetsReference(jsOptStrategy.directoryName, "js")
        if (jsIts.nonEmpty) {
          val jsItsScripts = jsIts.map(it => s"""<script src="$it"></script>""").mkString("\n")

          val indexContent = index
            .copy(
              refFonts = Seq(fontsU, fontsRefs),
              refCss = Seq(cssU),
              refJs = Seq(jsU, jsItsScripts)
            )
            .generate
          val indexFilePath = webpackWorkingFolder.value.toPath / jsOptStrategy.indexFileName
          NioFiles.writeString(indexFilePath, indexContent, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
          Some(indexFilePath)
        } else None
      }
      resolved.flatMap(_.toSeq).headOption.foreach { it =>
        IOFiles.copyFile(it, indexPath)
      }
      indexPath
    },
    webpack := {
      val log = streams.value.log.asScala
      log.info("[webpack] starting")
    },
    webpack := webpack
      .dependsOn(webpackGenerateIndex)
      .dependsOn(webpackCurrentProject)
      .dependsOn(webpackScalaJsProject)
      .dependsOn(webpackCollectDependencies)
      .value
  )
}
