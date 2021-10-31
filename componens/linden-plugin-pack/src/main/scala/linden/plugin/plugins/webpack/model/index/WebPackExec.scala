package linden.plugin.plugins.webpack.model.index

import linden.plugin.utils.{Log, WebPack}
import linden.plugin.utils.io.IOFiles
import sbt.Keys.{resourceDirectory, resourceManaged, scalaVersion, target}
import sbt.{SettingKey, pathToPathOps}

import java.io.File
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import java.nio.file.{Path, Files => NioFiles}

case class WebPackSource(
  workingAssetsFolder: Path,
  scalaJsSourceFolder: Path,
  webpackBaseSourceFolder: Seq[Path]
)

object WebPackExec {

  def go(webPackSources: Seq[WebPackSource])(implicit log: Log, ec: ExecutionContext): Future[Seq[Path]] =
    Await.ready(Future.sequence(webPackSources.map(it => go(it))).map(_.flatten),Duration.Inf)

  def go(webPackSource: WebPackSource)(implicit log: Log, ec: ExecutionContext): Future[Seq[Path]] =
    go(webPackSource.workingAssetsFolder, webPackSource.scalaJsSourceFolder, webPackSource.webpackBaseSourceFolder)

  def go(workingFolder: Path, scalaJsSourceFolder: Path, webpackBaseSourceFolder: Seq[Path])(implicit log: Log, ec: ExecutionContext): Future[Seq[Path]] = {

    webpackBaseSourceFolder.foreach { base =>
      WebPack.marketDirectory.foreach { case (from, to) =>
        val resource = base / "webpack" / from
        IOFiles.copyFiles(resource, workingFolder / to)
      }
    }
    /* WebPack.marketDirectory.foreach { case (from, to) =>
       val resourceWebpack = (resourceManaged in Compile).value.toPath / "webpack" / from
       IOFiles.copyFiles(resourceWebpack, webPackFolder / to)

       val resourceDirectoryWebpack = (resourceDirectory in Compile).value.toPath / "webpack" / from
       IOFiles.copyFiles(resourceDirectoryWebpack, webPackFolder / to)
     }*/


    Future.sequence(
      WebPack.jsOptStrategies.map { jsOptStrategy =>
        Future {
          val jsFolder = workingFolder / jsOptStrategy.directoryName
          NioFiles.createDirectories(jsFolder)
          //val targetFolder = (target in Compile).value.toPath / s"scala-${scalaBinaryVersion.value}"
          val targetFolder = scalaJsSourceFolder
          log.info(s"[webpackCurrentProject] Move javascript files from $targetFolder")
          IOFiles.list(targetFolder)
            .filter(it => NioFiles.isRegularFile(it))
            .filter(_.getFileName.toString.endsWith(".js"))
            .filter(jsOptStrategy.is)
            .map { it =>
              IOFiles.copyFile(it, jsFolder)
            }
        }
      }
    ).map(_.flatten)
  }
}
