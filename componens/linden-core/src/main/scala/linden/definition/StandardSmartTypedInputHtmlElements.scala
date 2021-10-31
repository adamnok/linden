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

package linden.definition

import linden.definition.StandardSmartTypedInputHtmlElements.InputType
import linden.store.ReadStore
import linden.store.functional.Real
import linden.store.mutable.SimpleStore
import linden.store.functional.Action.implicitConvertForReal
import scala.language.implicitConversions

object StandardSmartTypedInputHtmlElements:

  trait InputType[T]:
    type EE = T
    def use(identity: String, store: T)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit

  given InputTypeForSimpleStoreStringString: InputType[SimpleStore[String, String]] with
    override def use(identity: String, store: EE)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit =
      creator.inputText(identity, store)(content)

  given InputTypeForReadStoreString: InputType[ReadStore[String] & Real[SimpleStore[String, String]]] with
    override def use(identity: String, store: EE)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit =
      creator.inputText(identity, store)(content)

  given InputTypeForSimpleStoreStringOptionString: InputType[SimpleStore[String, Option[String]]] with
    override def use(identity: String, store: EE)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit =
      creator.inputTextByForm(identity, store)(content)

  given InputTypeForSimpleStoreOptionStringOptionString: InputType[SimpleStore[Option[String], Option[Option[String]]]] with
    override def use(identity: String, store: EE)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit =
      creator.inputTextByOptForm(identity, store)(content)

  given InputTypeForSimpleStoreIntInt: InputType[SimpleStore[Int, Int]] with
    override def use(identity: String, store: EE)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit =
      creator.inputNumber(identity, store)(content)

  given InputTypeForReadStoreIntInt: InputType[ReadStore[Int] & Real[SimpleStore[Int, Int]]] with
    override def use(identity: String, store: EE)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit =
      creator.inputNumber(identity, store)(content)

  given InputTypeForSimpleStoreIntOptionInt: InputType[SimpleStore[Int, Option[Int]]] with
    override def use(identity: String, store: EE)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit =
      creator.inputNumberByForm(identity, store)(content)

  given InputTypeForSimpleStoreBooleanBoolean: InputType[SimpleStore[Boolean, Boolean]] with
    override def use(identity: String, store: EE)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit =
      creator.inputCheckbox(identity, store)(content)

  given InputTypeForReadStoreBooleanBoolean: InputType[ReadStore[Boolean] & Real[SimpleStore[Boolean, Boolean]]] with
    override def use(identity: String, store: EE)(content: => Unit)(using context: Context, creator: StandardInputHtmlElements): Unit =
      creator.inputCheckbox(identity, store)(content)

trait StandardSmartTypedInputHtmlElements:
  creator: StandardInputHtmlElements =>

  implicit private val creator_ : StandardInputHtmlElements = creator

  def input[T](store: T)(implicit context: Context, inputType: InputType[T]): Unit =
    inputType.use("", store){}

  def inputCustom[T](store: T)(content: => Unit)(implicit context: Context, inputType: InputType[T]): Unit =
    inputType.use("", store)(content)

  def input[T](identity: String, store: T)(implicit context: Context, inputType: InputType[T]): Unit =
    inputType.use(identity, store){}

  def inputCustom[T](identity: String, store: T)(content: => Unit)(implicit context: Context, inputType: InputType[T]): Unit =
    inputType.use(identity, store)(content)
