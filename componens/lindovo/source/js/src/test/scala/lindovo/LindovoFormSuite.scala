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
import lindovo.{Validator, FieldValidator, Failed}
import scala.concurrent.ExecutionContext.Implicits._
import lindovo.FormMapping


object LindovoFormSuite extends SimpleTestSuite:
 
  testAsync("Simple validator test #1") {
    // Given
    case class User(id: Int, name: String)
    case class UserWorkTemplate[T[_]](id: T[Int], name: T[String])

    given FormMapping[User, UserWorkTemplate]()
    def min(v: Int)(it: Int) = Option.when(it < v)(s"Legalább $v értékűnek kell lennie!")
    def max(v: Int)(it: Int) = Option.when(it > v)(s"Legfeljebb $v értékű lehet!")

    val user = User(11, "")
    val form = LindovoForm(user) { m =>
      import m.*
      copy(
        id = id.register(min(10), max(20))
      )
    }

    // When
    val validated = form.validated

    // Then
    validated map { validatedUser =>
      assertEquals(Some(user), validatedUser)
    }
  }

  testAsync("Simple validator test #2") {
    // Given
    case class User(id: Int, name: String)
    case class UserWorkTemplate[T[_]](id: T[Int], name: T[String])

    given FormMapping[User, UserWorkTemplate]()
    def min(v: Int)(it: Int) = Option.when(it < v)(s"Legalább $v értékűnek kell lennie!")
    def max(v: Int)(it: Int) = Option.when(it > v)(s"Legfeljebb $v értékű lehet!")

    val user = User(3, "")
    val form = LindovoForm(user) { m =>
      import m.*
      copy(
        id = id.register(min(10), max(20))
      )
    }

    // When
    val validated = form.validated

    // Then
    validated map { validatedUser =>
      assertEquals(None, validatedUser)
    }
  }

  testAsync("Simple validator test #3") {
    // Given
    import lindovo.fieldvalidators.IntFieldValidators.MinNumber
    import lindovo.FieldValidators.*

    case class User(id: Int, name: String)
    case class UserWorkTemplate[T[_]](id: T[Int], name: T[String])

    given FormMapping[User, UserWorkTemplate]()
   
    val user = User(3, "")
    val form = LindovoForm(user) { m =>
      import m.*
      copy(
        id = id.register(minNumber(10), maxNumber(20))
      )
    }

    // When
    val validated = form.getValidated

    // Then
    validated map { result =>
      assert(result.isRight)
      val failed: UserWorkTemplate[Failed] = result.toOption.get
      val idFailed: Failed[Int] = failed.id
      val nameFailed: Failed[String] = failed.name
      assertEquals(idFailed.value, user.id)
      assertEquals(idFailed.errors, Seq(MinNumber(10)))
      assertEquals(nameFailed.value, user.name)
      assertEquals(nameFailed.errors, Seq())
    }
  }

  testAsync("Simple validator with optional field #1") {
    case class User(id: Option[Int], name: String)
    case class UserWorkTemplate[T[_]](id: T[Option[Int]], name: T[String])

    given FormMapping[User, UserWorkTemplate]()

    val user = User(Some(3), "")
    val form = LindovoForm(user)

    // When
    val validated = form.validated

    // then
    validated map { validatedUser =>
      assertEquals(Some(user), validatedUser)
    }
  }

   testAsync("Simple validator with optional field #2") {
    case class User(id: Option[Int], name: String)
    case class UserWorkTemplate[T[_]](id: T[Option[Int]], name: T[String])

    given FormMapping[User, UserWorkTemplate]()

    val user = User(None, "")
    val form = LindovoForm(user)

    // When
    val validated = form.validated

    // then
    validated map { validatedUser =>
      assertEquals(Some(user), validatedUser)
    }
  }

  testAsync("Simple validator with required field #1") { // TODO így nincs értelme
    case class User(id: Int, name: String)
    case class UserWorkTemplate[T[_]](id: T[Int], name: T[String])

    def builder(m: UserWorkTemplate[FieldValidator]) =
      import m.*
      copy(
        id = id.register { it =>
          //println(s"IT it=${it}")
          None
        }
      )

    given FormMapping[User, UserWorkTemplate](builder)

    val user = User(0, "")
    val form = LindovoForm(user)

    // When
    val validated = form.validated

    // then
    validated map { validatedUser =>
      assertEquals(Some(user), validatedUser)
    }
  }
