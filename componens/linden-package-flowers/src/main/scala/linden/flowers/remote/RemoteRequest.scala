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

package linden.flowers.remote

import linden.flowers.Experimental
import linden.util.dsl.*
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.XMLHttpRequest

import scala.concurrent.{ExecutionContext, Future}

@Experimental
object RemoteRequest {
  private var cacheMap = Map.empty[String, Any]

  private def cache[T](key: String, isActive: Boolean)(create: => Future[T]): Future[T] =
    if (isActive) synchronized {
      cacheMap.get(key) match {
        case None => create.also(cacheMap += key -> _)
        case Some(it) => it.asInstanceOf[Future[T]]
      }
    } else {
      create
    }

  def get(url: String)(implicit ec: ExecutionContext): Future[String] =
    Ajax
      .get(
        url = url,
        withCredentials = true,
        headers = Map()
      )
      .map(_.responseText)
}

@Experimental
trait RemoteRequest { //[R,W,E] {

  type ReaderType[T]
  type WriterType[T]
  type ErrorType

  def withExpected[T: ReaderType]() = new Expectation[T]

  def argumentsRead[T: ReaderType](it: String): T

  def argumentsReadError(it: String): ErrorType

  def argumentsWrite[T: WriterType](it: T): String

  class Expectation[T: ReaderType] {
    def post[G: WriterType](url: String, data: G)(implicit ec: ExecutionContext): Future[T] =
      nativePost[T, G](url, data)

    def get(url: String, cache: Boolean = false)(implicit ec: ExecutionContext): Future[T] =
      nativeGet[T](url, cache)

    def delete(url: String)(implicit ec: ExecutionContext): Future[T] =
      nativeDelete[T](url)

    def put(url: String)(implicit ec: ExecutionContext): Future[T] =
      nativePut[T](url)
  }

  private def nativePost[T: ReaderType, G: WriterType](url: String, data: G)(implicit ec: ExecutionContext): Future[T] =
    Ajax
      .post(
        url = url,
        data = InputData.str2ajax(argumentsWrite[G](data)),
        withCredentials = true,
        headers = Map()
      )
      .map(ajaxSuccess[T])
      .recoverWith(ajaxRecover)

  private def nativeGet[T: ReaderType](url: String, cache: Boolean)(implicit ec: ExecutionContext): Future[T] =
    RemoteRequest.cache(url, cache) {
      RemoteRequest.get(url)
        .map(ajaxSuccessText[T])
        .recoverWith(ajaxRecover)
    }

  private def nativeDelete[T: ReaderType](url: String)(implicit ec: ExecutionContext): Future[T] =
    Ajax
      .delete(
        url = url,
        withCredentials = true,
        headers = Map()
      )
      .map(ajaxSuccess[T])
      .recoverWith(ajaxRecover)

  private def nativePut[T: ReaderType](url: String)(implicit ec: ExecutionContext): Future[T] =
    Ajax
      .put(
        url = url,
        withCredentials = true,
        headers = Map()
      )
      .map(ajaxSuccess[T])
      .recoverWith(ajaxRecover)

  @inline
  private def ajaxSuccessText[T: ReaderType](content: String): T =
    argumentsRead[T](content)

  @inline
  private def ajaxSuccess[T: ReaderType](xmlHttpRequest: XMLHttpRequest): T =
    argumentsRead[T](xmlHttpRequest.responseText)

  @inline
  private def ajaxRecover[T]: PartialFunction[Throwable, Future[T]] = {
    case AjaxException(xmlHttpRequest) =>
      Future failed RemoteRequestException(argumentsReadError(xmlHttpRequest.responseText))
  }

  case class RemoteRequestException(errors: ErrorType) extends Exception

}