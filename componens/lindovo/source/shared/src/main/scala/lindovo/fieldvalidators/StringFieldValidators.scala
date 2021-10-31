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

package lindovo.fieldvalidators

import lindovo.FieldError
import scala.language.implicitConversions
import StringFieldValidators.*

object StringFieldValidators:

  trait StringFieldError extends FieldError

  case object IsRequire extends StringFieldError:
    override def message = "lindovo.field(:string).IsRequire"

  case object IsEmail extends StringFieldError:
    override def message = "lindovo.field(:string).IsEmail"

  case class MinTextLength(length: Int) extends StringFieldError:
    override def message = s"lindovo.field(:string).MinTextLength($length)"

  case class MaxTextLength(length: Int) extends StringFieldError:
    override def message = s"lindovo.field(:string).MaxTextLength($length)"

  case object OnlyEnglishCharacters extends StringFieldError:
    override def message = "lindovo.field(:string).OnlyEnglishCharacters"

  case object StartTextWithUpperCase extends StringFieldError:
    override def message = "lindovo.field(:string).StartTextWithUpperCase"

  case class NotAllowedTexts(notAllowed: Set[String]) extends StringFieldError:
    override def message = s"lindovo.field(:string).NotAllowedTexts($notAllowed)"

trait StringFieldValidators:
  extension (it: String)
    inline def nonMatches(regex: String): Boolean = !(it matches regex)

  def isRequire: String => Option[FieldError] = {
    case "" => Some(IsRequire)
    case _ => None
  }

  def isEmail: String => Option[FieldError] = { it =>
    Option.when(
      Seq(
        it.trim.nonEmpty,
        it nonMatches "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
      ).forall(identity[Boolean])
    )(IsEmail)
  }

  def minTextLength(length: Int)(it: String) =
    Option.when(it.length < length)(MinTextLength(length))

  def maxTextLength(length: Int)(it: String) =
    Option.when(it.length > length)(MaxTextLength(length))

  def onlyEnglishCharacters()(it: String) =
    Option.unless(it matches "[ a-zA-Z]*")(OnlyEnglishCharacters)

  def startTextWithUpperCase()(it: String) =
    val firstLetter = it.head.toString
    Option.when(firstLetter != firstLetter.toUpperCase)(StartTextWithUpperCase)

  def notAllowedTexts(notAllowed: Set[String])(it: String) =
    Option.when(notAllowed contains it)(NotAllowedTexts)