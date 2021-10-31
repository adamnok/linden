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

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.language.higherKinds
import scala.deriving.Mirror

object Validator {

 

  //private[lindovo] def join[T](validators: Seq[Validator[T]])(using ExecutionContext) =
  //  new JoinedValidator[T](validators)

  /**
   * Composing many Validator Entity Builder
   */
  private[lindovo] def compose[E[H[_]] <: Product](builders: Seq[E[FieldValidator] => E[FieldValidator]])(
    using Mirror.ProductOf[E[FieldValidator]]
  ): E[FieldValidator] => E[FieldValidator] =
    (input) => compose(builders.map(_.apply(input)))

  /**
   * Composing many Validator Entity
   */
  private[lindovo] def compose[E[H[_]] <: Product](validators: Seq[E[FieldValidator]])(
    using Mirror.ProductOf[E[FieldValidator]]
  ): E[FieldValidator] =
    val fields = validators
      .map(_.productIterator.toSeq.asInstanceOf[Seq[FieldValidator[Any]]])
      .reduceLeft((acc, it) => (acc zip it) map (_ + _))
      .toArray
    summon[Mirror.ProductOf[E[FieldValidator]]].fromProduct(Tuple.fromArray(fields))



}
