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

import lindovo.{Validator, Failed}

import scala.concurrent.Future
import scala.language.higherKinds
import scala.deriving.Mirror
import lindovo.internal.LindovoMacro
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import lindovo.FieldError
import lindovo.FieldValidator
import lindovo.internal.FieldsTypeFetcher

trait FormMappable[A <: Product]:
  type O[H[_]] <: Product
  val baseBuilders: Seq[O[FieldValidator] => O[FieldValidator]]

/**
 * ```
 * case class User(id: Int, name: String)
 * case class UserWorkTemplate[T[_]](id: T[Int], name: T[String])
 * given FormMapping[User, UserWorkTemplate]() // if you don't need default base builder
 *
 * val builder: UserWorkTemplate[Validator] => UserWorkTemplate[Validator] = ...
 * given FormMapping[User, UserWorkTemplate](builder) // if you have default base builder
 * ```
 */
trait FormMapping[A <: Product, B[H[_]] <: Product](
  override val baseBuilders: (B[FieldValidator] => B[FieldValidator])*
) extends FormMappable[A]:
  override type O[H[_]] = B[H]

object LindovoForm:

  inline def apply[S <: Product](
    using t: FormMappable[S]
  )(
    value: S
  )(
    using
      ExecutionContext,
      Mirror.ProductOf[S],
      Mirror.ProductOf[t.O[Failed]],
      Mirror.ProductOf[t.O[FieldValidator]]
  )(
    using inline fieldTypes: FieldsTypeFetcher = FieldsTypeFetcher.byType[S]
  ) = new LindovoForm[S, t.O](value, t.baseBuilders){}

  private[LindovoForm] def validate[S <: Product, E[_[_]] <: Product](
    useCache: Boolean,
    builder: E[FieldValidator]
  )(
    using
      ExecutionContext,
      Mirror.ProductOf[S],
      Mirror.ProductOf[E[Failed]],
      Mirror.ProductOf[E[FieldValidator]]
  ): Future[Either[S, E[Failed]]] =
    Future
      .sequence(
        builder.productIterator
          .map(x => x.asInstanceOf[FieldValidator[Any]])
          .map(x => x.value().flatMap(y => x.errors(useCache).map(_ -> y)))
      )
      .map(_.toList)
      .map { it =>
        if (it.forall(_._1.isEmpty)) {
          val fields = it.map(_._2).toArray
          val value = summon[Mirror.ProductOf[S]].fromProduct(Tuple.fromArray(fields))
          Left(value)
        } else {
          val fields = it.map(_.swap).toArray
            .map((value, errors) => Failed[Any](value, errors))
            .toArray
          val value = summon[Mirror.ProductOf[E[Failed]]].fromProduct(Tuple.fromArray(fields))
          Right(value)
        }
      }

trait LindovoForm[S <: Product, E[H[_]] <: Product](
  value: S,
  builders: Seq[E[FieldValidator] => E[FieldValidator]]
)(
  using fieldTypes: FieldsTypeFetcher
)(using
    ExecutionContext,
    Mirror.ProductOf[S],
    Mirror.ProductOf[E[Failed]],
    Mirror.ProductOf[E[FieldValidator]]
):

  def apply(moreBuilders: E[FieldValidator] => E[FieldValidator]*): LindovoForm[S, E] =
    new LindovoForm[S, E](value, builders ++ moreBuilders.asInstanceOf[Seq[E[FieldValidator] => E[FieldValidator]]]){}

  private def createEntityValidator: E[FieldValidator] =
    val fields = Tuple.fromProductTyped(value).toArray
      .zip(fieldTypes().map(_ matches "scala.Option\\[.*\\]"))
      .map { (value, optional) =>
        FieldValidator[Any](optional, () => Future.successful(value))
      }
      .toArray
    summon[Mirror.ProductOf[E[FieldValidator]]].fromProduct(Tuple.fromArray(fields))

  def validator: E[FieldValidator] = builders match
    case Seq() => createEntityValidator
    case Seq(builder) => builder(createEntityValidator)
    case builders => Validator.compose(builders)(createEntityValidator)

  /**
   * Validating the entity object and response it.
   *
   * @return
   *    - None: values of entity where incorrect
   *    - Some(value): the entity with non-changed values
   */
  def validated: Future[Option[S]] =
    LindovoForm.validate[S, E](true, validator).map(_.swap.toOption)

  def getValidated: Future[Either[S, E[Failed]]] =
    LindovoForm.validate[S, E](false, validator)

  def getValidatedNative: Future[Either[S, Array[(Any, Seq[FieldError])]]] =
    getValidated
      .map {
        _ map { (it: E[Failed]) => 
          Tuple.fromProductTyped(it).toArray
            .map(_.asInstanceOf[Failed[_]])
            .map(it => it.value -> it.errors)
            .toArray
        }
      }

end LindovoForm
