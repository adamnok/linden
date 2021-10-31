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

package linden.di

import minitest.*

import linden.di.SessionDI

object DIMacrosSuite extends SimpleTestSuite:

  test("A full di-session test for DIMacros.createInstanceReverseArgs") {
    // given
    val session = new SessionDI:
      def apply[T](a: String, n: String): T = get[T](a, n).get
      def get[T](a: String, n: String): Option[T] =
        val value = (a, n) match
          case ("scala.Predef.String", "name") => Some("Anastasia")
          case ("scala.Int", "id") => Some(3)
          case _ => None
        value.asInstanceOf[Option[T]]
    case class User(id: Int, name: String)

    // when
    val user = DIMacros.createInstanceReverseArgs[User](session)

    // then
    assertEquals(user.id, 3)
    assertEquals(user.name, "Anastasia")
  }

   test("A full di-session failure test for DIMacros.createInstanceReverseArgs") {
    // given
    val session = new SessionDI:
      def apply[T](a: String, n: String): T = get[T](a, n).get
      def get[T](a: String, n: String): Option[T] =
        val value = (a, n) match
          case ("scala.Predef.String", "name") => Some("Anastasia")
          case _ => None
        value.asInstanceOf[Option[T]]
    case class User(id: Int, name: String)

    // when
    intercept[IllegalArgumentException] {
      val user = DIMacros.createInstanceReverseArgs[User](session)
    }

    // then exception
    assert(true)
  }

  test("A full di-session test for DIMacros.createInstanceReverseArgs with parameters") {
    // given
    val session = new SessionDI:
      def apply[T](a: String, n: String): T = get[T](a, n).get
      def get[T](a: String, n: String): Option[T] =
        val value = (a, n) match
          case ("scala.Predef.String", "name") => Some("Anastasia")
          case ("scala.Int", "id") => Some(3)
          case _ => None
        value.asInstanceOf[Option[T]]
    class User(val values: Seq[Int])(val id: Int, val name: String)

    // when
    val values = Seq(1, 2, 3)
    val user = DIMacros.createInstanceReverseArgs[User](session)(values)

    // then
    assertEquals(user.id, 3)
    assertEquals(user.name, "Anastasia")
    assertEquals(user.values, Seq(1, 2, 3))
  }

   test("DIMacros.createInstanceReverseArgs with Generics type") {
    // given
    val session = new SessionDI:
      def apply[T](a: String, n: String): T = get[T](a, n).get
      def get[T](a: String, n: String): Option[T] =
        val value = (a, n) match
          case ("scala.Predef.String", "name") => Some("Anastasia")
          case ("scala.Int", "id") => Some(3)
          case _ => None
        value.asInstanceOf[Option[T]]
    class User[T](val values: Seq[T])(val id: Int, val name: String)

    // when
    val values = Seq(1, 2, 3)
    val user = DIMacros.createInstanceReverseArgs[User[Int]](session)(values)

    // then
    assertEquals(user.id, 3)
    assertEquals(user.name, "Anastasia")
    assertEquals(user.values, Seq(1, 2, 3))
  }