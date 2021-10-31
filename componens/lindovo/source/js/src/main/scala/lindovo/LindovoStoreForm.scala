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

import linden.store.mutable.SimpleStore
import linden.store.{ClearContext, ReadStore, Store}
import lindovo.*

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.language.higherKinds
import scala.deriving.Mirror
import linden.store.mutable.ForceUpdateStore
import linden.store.mutable.ForceUpdateStore.NeverEQ
import LindovoStoreForm.*
import lindovo.internal.LindovoMacro
import lindovo.internal.FieldsTypeFetcher

case object InputStoreIsUntouchedException extends Exception

object LindovoStoreForm:
  class NeverEQ:
    override def equals(o: Any) = false
  type FormStoreItem[T] = SimpleStore[T, Option[T]]
  type ValidatedStoreItem[T] = SimpleStore[Validated[T], Validated[T]]
  type ValidatedReadStoreItem[T] = ReadStore[Validated[T]]
  
  inline def apply[S <: Product](
    using t: FormMappable[S]
  )(using
      ExecutionContext,
      Mirror.ProductOf[S],
      Mirror.ProductOf[t.O[Failed]],
      Mirror.ProductOf[t.O[FormStoreItem]],
      Mirror.ProductOf[t.O[FieldValidator]],
      Mirror.ProductOf[t.O[ValidatedStoreItem]],
      Mirror.ProductOf[t.O[ValidatedReadStoreItem]]
  )(
    using inline fieldTypes: FieldsTypeFetcher = FieldsTypeFetcher.byType[S]
  ) = new LindovoStoreForm[S, t.O](t.baseBuilders){}

  case class Options(
    useInitForValidatedStoreItem: Boolean = true,
    useInitForValidatedItemStore: Boolean = true,
    externalStores: Seq[ReadStore[?]] = Seq()
  )

abstract class LindovoStoreForm[S <: Product, E[_[_]] <: Product](
  baseBuilders: Seq[E[FieldValidator] => E[FieldValidator]]
)(
  using fieldTypes: FieldsTypeFetcher
)(using
    ExecutionContext,
    Mirror.ProductOf[S],
    Mirror.ProductOf[E[Failed]],
    Mirror.ProductOf[E[FormStoreItem]],
    Mirror.ProductOf[E[FieldValidator]],
    Mirror.ProductOf[E[ValidatedStoreItem]],
    Mirror.ProductOf[E[ValidatedReadStoreItem]]
){
  type Type = S
  type Source = S
  type FormStore = E[FormStoreItem]
  type FormValidator = E[FieldValidator]
  type FormValidatedStore = E[ValidatedStoreItem]
  type FormValidatedReadStore = E[ValidatedReadStoreItem]
  type ValidatedStore = SimpleStore[Option[Type], Option[Type]]

  protected def store: E[FormStoreItem] =
    val fields = (0 to fieldTypes().length).toArray
      .map(_ => Store.option[Any])
      .toArray
    summon[Mirror.ProductOf[E[FormStoreItem]]].fromProduct(Tuple.fromArray(fields))

  protected def storeValidator(store: E[FormStoreItem]): E[FieldValidator] =
    val fields = Tuple.fromProductTyped(store).toArray
      .map(_.asInstanceOf[ReadStore[Option[_]]])
      .zip(fieldTypes().map(_ startsWith "scala.Option"))
      .map { case (store, optional) =>
        optional -> {
          () => store.value match
            case None if optional => Future.successful(None)
            case None => Future.failed(InputStoreIsUntouchedException)
            case Some(value) => Future.successful(value)
        }
      }
      .map { case (optional, fn) =>
        FieldValidator[Any](optional,  fn)
      }
    summon[Mirror.ProductOf[E[FieldValidator]]].fromProduct(Tuple.fromArray(fields))

  protected def validatedStore(store: E[FormStoreItem]): FormValidatedStore =
    val fields = Tuple.fromProductTyped(store).toArray
      .map(_.asInstanceOf[ReadStore[Option[_]]])
      .map(_.value)
      .map(it => Store(Validated(it, Seq.empty[FieldError])))
    summon[Mirror.ProductOf[FormValidatedStore]].fromProduct(Tuple.fromArray(fields))

  protected def fromArrayToItem(array: Array[Any]): Source =
    summon[Mirror.ProductOf[Source]].fromProduct(Tuple.fromArray(array))

  protected def fromArrayToValidator(array: Array[FieldValidator[Any]]): E[FieldValidator] =
    summon[Mirror.ProductOf[E[FieldValidator]]].fromProduct(Tuple.fromArray(array))

  private def use: (FormStore, E[FieldValidator], FormValidatedStore, ValidatedStore) =
    val currentStore = store
    val currentValidator = storeValidator(currentStore)
    val currentValidatedStore = validatedStore(currentStore)
    val currentValidatedItemStore = Store[Option[Type]](None)
    (currentStore, currentValidator, currentValidatedStore, currentValidatedItemStore)

  private def nativeCreateFormStore(
    builder: E[FieldValidator] => E[FieldValidator]
  )(
    using clearContext: ClearContext,  options: Options
  ): StoreCollectForForm[S, E] =
    val forceStore = Store.forceUpdate()
   
    val (store, validatorBuilder, validatedStore, validatedItemStore) = use
    val validator = builder(validatorBuilder)

    setupStoreForm(forceStore)(store, validatorBuilder, validatedStore, validatedItemStore)(fromArrayToItem)(validator)

    val lindovoForm = createSimpleValidatedFn(builder, validatedItemStore.value, getCurrentValue(store))(forceStore.forceUpdate)

    //val (storeR, validatorBuilderR, validatedStoreR, validatedItemStoreR) =
    //  revisionAfterSetup(store, validatorBuilder, validatedStore, validatedItemStore)
    //StoreCollectForForm[S, E](storeR, validatedStoreR.asInstanceOf[FormValidatedReadStore], validatedItemStoreR)
    StoreCollectForForm[S, E](
      store = store,
      formValidatedStore = validatedStore.asInstanceOf[FormValidatedReadStore],
      validatedStore = validatedItemStore,
      validated = lindovoForm
    )

  private def getCurrentValue(store: FormStore): Option[S] =
    try
      val fields = Tuple.fromProductTyped(store).toArray
        .map(_.asInstanceOf[ReadStore[Option[_]]])
        .map(_.value)
        .map {
          case None => throw InputStoreIsUntouchedException
          case Some(v) => v
        }
      Some(summon[Mirror.ProductOf[S]].fromProduct(Tuple.fromArray(fields)))
    catch
      case InputStoreIsUntouchedException => None

  private def createSimpleValidatedFn(
    builder: E[FieldValidator] => E[FieldValidator],
    entity: => Option[S],
    entityReCheck: => Option[S]
  )(reRequest: () => Unit): () => Future[Option[S]] =
    () =>
      entity match
        case None =>
          entityReCheck match
            case None => Future.successful(None)
            case Some(it) =>
              (new LindovoForm[S, E](it, Seq(builder)){})
                .validated
                .map {
                  case None => None
                  case it => reRequest(); it
                }
        case Some(it) =>
          (new LindovoForm[S, E](it, Seq(builder)){})
            .validated
            .map {
              case None => reRequest(); None
              case it => it
            }

  def apply(
    builders: (E[FieldValidator] => E[FieldValidator])*
  )(
    using clearContext: ClearContext
  )(
    using options: Options = Options()
  ): StoreCollectForForm[S, E] =
    nativeCreateFormStore(Validator.compose(baseBuilders ++ builders))

  private def setupStoreForm(forceUpdateStore: ForceUpdateStore)(
    store: E[FormStoreItem],
    validatorBuilder: E[FieldValidator],
    formValidatedStores: E[ValidatedStoreItem],
    validatedStore: ValidatedStore
  )(
    fromArrayToItem: Array[Any] => S
  )(
    validator: E[FieldValidator]
  )(
    using clearContext: ClearContext, options: Options
  ): Unit = {
    val storeOfFields = Store.sequence(store.productIterator.toSeq.asInstanceOf[Seq[ReadStore[Option[Any]]]])
    val forceUpdate = forceUpdateStore.zipWithLastValue(NeverEQ).map(_ ne _)
    val externalStores = Store.sequence[Any](options.externalStores.asInstanceOf[Seq[ReadStore[Any]]])
    Store.join(forceUpdate, externalStores, storeOfFields).lateSubscribe { case (force, _, (it: Seq[Option[Any]])) =>
      val validatorAsSeq: Seq[FieldValidator[Any]] = validator.productIterator.toSeq
        .asInstanceOf[Seq[FieldValidator[Any]]]

      val validatedStoreAsSeq: Seq[ValidatedStoreItem[Any]] = formValidatedStores.productIterator.toSeq
        .asInstanceOf[Seq[ValidatedStoreItem[Any]]]

      val forAll = (it zip validatorAsSeq zip validatedStoreAsSeq)
        .zipWithIndex
        .map {
          case (((valueOpt, validatorItem), validatedStoreItem), index) =>
            valueOpt match
              case Some(Some("")) if validatorItem.optional =>
                Future(true -> index -> None)
              case Some(value) =>
                println(s"force = $force")
                validatorItem.errors(useCache = !force).map {
                  case Seq() =>
                    validatedStoreItem.change(Validated(Some(value), Seq()))
                    true -> index -> value
                  case seq =>
                    validatedStoreItem.change(Validated(Some(value), seq))
                    false -> index -> value
                }
              case None =>
                Future(validatorItem.optional -> index -> None)
        }
      Future.sequence(forAll)
        .map(col => col.forall(x => x._1._1) -> col.map(x => x._1._2 -> x._2))
        .foreach {
          case (true, columns: Seq[(Int, Any)]) =>
            //throw new IllegalStateException(columns.toString())
            val realColumns = columns.sortBy(_._1).map(_._2)
            validatedStore.change(Some(fromArrayToItem(realColumns.toArray)))
          case (false, _) =>
            validatedStore.change(None)
        }
    }
  }

  type RevisionedValidatedStore = ReadStore[Option[Source]]
  type RevisionedFormValidatedStore = E[ValidatedReadStoreItem]
  private def revisionAfterSetup(
    store: FormStore,
    validatorBuilder: FormValidator,
    formValidatedStores: FormValidatedStore,
    validatedStore: ValidatedStore
  )(
    using options: Options = Options()
  ): (FormStore, FormValidator, RevisionedFormValidatedStore, RevisionedValidatedStore) =
    val revisionedValidatedStore = options.useInitForValidatedItemStore match
      case false => validatedStore
      case true  => validatedStore init None
    val revisionedFormValidatedStores = {
      val fields = Tuple.fromProductTyped(formValidatedStores).toArray
      .map(_.asInstanceOf[ReadStore[Validated[_]]])
      .map { it =>
        options.useInitForValidatedStoreItem match
          case false => it
          case true  => it init Validated.empty
      }
      summon[Mirror.ProductOf[RevisionedFormValidatedStore]].fromProduct(Tuple.fromArray(fields))
    }
    (store, validatorBuilder, revisionedFormValidatedStores, revisionedValidatedStore)
}
