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

import scala.concurrent.{Future, ExecutionContext}
import scala.language.higherKinds

import linden.store.ReadStore
import linden.store.mutable.SimpleStore
import lindovo.LindovoStoreForm.{FormStoreItem, ValidatedReadStoreItem}

case class StoreCollectForForm[S <: Product, E[_[_]] <: Product](
  store: E[FormStoreItem],
  formValidatedStore: E[ValidatedReadStoreItem],
  validatedStore: ReadStore[Option[S]],
  validated: () => Future[Option[S]]
):

  def get: Option[S] = validatedStore.value

  def validatedOrError(using ExecutionContext): Future[S] =
    validated().flatMap {
      case None => Future.failed(throw IllegalArgumentException("None exception"))
      case Some(value) => Future.successful(value)
    }

