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

package linden.plugin

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

package object utils {

  def use[A <: AutoCloseable, R](a: A)(callback: A => R): R = {
    Try(callback(a))
      .recoverWith {
        case e: Exception =>
          Try(a.close())
          throw e
      }
      .get
  }

  def use[A <: AutoCloseable, B <: AutoCloseable, C](a: A, b: B)(callback: (A, B) => C): C = {
    Try(callback(a, b))
      .recoverWith {
        case e: Exception =>
          Try(a.close())
          Try(b.close())
          throw e
      }
      .get
  }

  def useFuture[A <: AutoCloseable, B <: AutoCloseable, C](a: A, b: B)(callback: (A, B) => Future[C]): Future[C] = {
    callback(a, b)
      .map { it =>
        Try(a.close())
        Try(b.close())
        it
      }
      .recoverWith {
        case e =>
          Try(a.close())
          Try(b.close())
          Future.failed(e)
      }
  }

  def use[A <: AutoCloseable, B <: AutoCloseable, C <: AutoCloseable, R](a: A, b: B, c: C)(callback: (A, B, C) => R): R = {
    Try(callback(a, b, c))
      .recoverWith {
        case e: Exception =>
          Try(a.close())
          Try(b.close())
          Try(c.close())
          throw e
      }
      .get
  }
}
