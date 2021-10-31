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

package linden.flowers

import scala.concurrent.ExecutionContext

import linden.component.{Component as CoreComponent}
import linden.di.DI
import linden.flowers.DIComponent

object Component:

  def factory = new ComponentInitMethods[Component]()

  /**
   * @since Linden Flower 1.0.5 (Experimental from 1.0.4)
   */
  inline def factoryBy[T <: Component]: DIComponent =
    (session: DI) => session.createInstance[T]

  /**
   * @since Linden Flower 1.0.5 (Experimental from 1.0.4)
   */
  inline def factoryBy[T <: Component](inline args: Any*): DIComponent =
    (session: DI) => session.createInstance[T](args*)

  inline def factoryBy[T <: Component](inline args1: Any*)(inline args2: Any*): DIComponent =
    (session: DI) => session.createInstance[T](args1*)(args2*)

  inline def factoryBy[T <: Component](inline args1: Any*)(inline args2: Any*)(inline args3: Any*): DIComponent =
    (session: DI) => session.createInstance[T](args1*)(args2*)(args3*)

abstract class Component extends CoreComponent[DI] with linden.flowers.definition.EventExtensions:
  type Html = IHtml
  
  given ec: ExecutionContext = ExecutionContext.global