package linden.plugin.utils.staticserver


import linden.plugin.utils.Log
import linden.plugin.utils.io.IOFiles

import java.nio.file.{Files, Path}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn

object StaticServer {

  import spark.Spark._

  private def copyExtra(indexPath: Path, extra: Seq[Path])(implicit log: Log): Future[Unit] = {
    Future.sequence(
      extra.map {
        case it if Files.notExists(it) =>
          Future(())
        case it if Files.isRegularFile(it) =>
          Future(IOFiles.copyFile(it, indexPath.getParent))
        case it if Files.isDirectory(it) =>
          Future(IOFiles.copyFiles(it, indexPath.getParent.resolve(it.getFileName.toString)))
      }
    ).map(_ => Unit)
  }

  private def startServer(indexPath: Path): Future[Unit] = {
    val portConfig = 8080

    port(portConfig)
    externalStaticFileLocation(indexPath.getParent.toAbsolutePath.toString)
    redirect.get("/", "/index.html");

    init()

    println(s"Server started on $portConfig")
    Future {
      println()
      println("Press any key to terminating the server")
      StdIn.readLine()
      stop()
      Unit
    }
  }

  def apply(indexPath: Path, extra: Seq[Path])(implicit log: Log): Future[Unit] = {
    copyExtra(indexPath, extra)
      .flatMap(_ => startServer(indexPath))
  }
}
