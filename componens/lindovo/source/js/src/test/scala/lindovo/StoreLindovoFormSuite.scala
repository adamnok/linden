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

package lindovo

import minitest.*
import scala.language.higherKinds
import linden.store.mutable.SimpleStore
import lindovo.*
import scala.concurrent.ExecutionContext.Implicits._

object StoreLindovoFormSuite extends SimpleTestSuite:

  case class User(id: Int, name: String)
  case class UserWorkTemplate[T[_]](id: T[Int], name: T[String])

  test("NEWNEWFORM BUG") {
    given FormMapping[User, UserWorkTemplate]()
    import linden.store.ClearContext

    given ClearContext = new ClearContext:
      def addCleared(callback: () => Unit): Unit = ()

    def min(v: Int)(it: Int) = 
      Option.when(it < v)(s"Legalább $v értékűnek kell lennie!")
    def max(v: Int)(it: Int) = 
      Option.when(it > v)(s"Legfeljebb $v értékű lehet!")

    val user = User(7, "")
    val form = LindovoStoreForm[User] { m =>
      import m.*
      copy(
        id = id.register(min(10), max(20))
      )
    }
    form.store.id.change(7)
    //form.store.name.change("elena")

    assert(1 == 1)
  }

  test("Simple validator with required field #1") {
    case class User(id: Int, name: String)
    case class UserWorkTemplate[T[_]](id: T[Int], name: T[String])

    import linden.store.ClearContext
    given ClearContext = new ClearContext:
      def addCleared(callback: () => Unit): Unit = ()

    def builder(m: UserWorkTemplate[FieldValidator]) =
      import m.*
      copy(
        id = id.register { it =>
          println(s"IT it=${it}")
          Option.empty[String]
        }
      )

    given FormMapping[User, UserWorkTemplate](builder)

    val storeForm = LindovoStoreForm[User]()

    println(storeForm.get)
    println(storeForm.formValidatedStore.id.value)
    assert(1 == 1)
  }