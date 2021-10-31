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

import java.io._
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object StreamGobbler {
  private def apply(id: String, inputStream: InputStream)(log: String => Unit) =
    new StreamGobbler(id, inputStream, log)

  def watch(id: String, process: Process)(log: Log)(out: OutputStream, error: OutputStream): Future[Unit] = {
    val errorGobbler = apply(id, process.getErrorStream) { line =>
      log.error(line)
      error.write(s"$id $$ $line\n".getBytes)
      error.flush()
    }
    val outGobbler = apply(id, process.getInputStream) { line =>
      log.info(line)
      out.write(s"$id $$ $line\n".getBytes)
      out.flush()
    }
    errorGobbler.start()
    outGobbler.start()
    Future {
      outGobbler.join()
      errorGobbler.join()
    }
  }

  def log(log: Log)(id: String, process: Process): Future[Unit] = {
    val errorGobbler = apply(id, process.getErrorStream)(it => log.error(it))
    val outGobbler = apply(id, process.getInputStream)(it => log.info(it))
    errorGobbler.start()
    outGobbler.start()
    Future {
      outGobbler.join()
      errorGobbler.join()
    }
  }

  def result(id: String, process: Process)(implicit log: Log): Future[String] = {
    val stringWriter = new StringWriter
    val errorGobbler = apply(id, process.getErrorStream)(it => log.error(it))
    val outGobbler = apply(id, process.getInputStream) { it =>
      stringWriter.write(it)
    }
    errorGobbler.start()
    outGobbler.start()
    Future {
      outGobbler.join()
      errorGobbler.join()
      stringWriter.toString.trim
    }
  }
}

class StreamGobbler(id: String, inputStream: InputStream, log: String => Unit) extends Thread {
  override def run(): Unit = {
    try {
      val inputStreamReader = new InputStreamReader(inputStream)
      val br = new BufferedReader(inputStreamReader)

      @tailrec
      def until(br: BufferedReader)(callback: String => Unit): Unit = {
        val line = Option(br.readLine())
        line.foreach(callback)
        if (line.isDefined) until(br)(callback)
      }

      until(br) { line =>
        log(s"$id $$ $line")
      }
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }
}