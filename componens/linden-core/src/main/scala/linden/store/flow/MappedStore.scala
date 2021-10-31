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

package linden.store.flow

import linden.store.{ClearContext, ReadStore}

/**
 * Eredeti értékét transzformált store.
 * Ezen store értékét megváltoztatni nem szabad.
 *
 * @tparam T Az eredeti store típusa.
 * @tparam G A transzformált érték típusa.
 * @param store A mappelendő store
 * @param transform Az értéktranszformáló függvény.
 */
class MappedStore[T, G](
  private val store: ReadStore[T],
  private val transform: T => G
) extends ReadStore[G]:

  override def value: G = transform(store.value)

  override def subscribe(callback: G => Unit)(using context: ClearContext): Unit =
    store.subscribe(it => callback(transform(it)))

  override def lateSubscribe(callback: G => Unit)(using context: ClearContext): Unit =
    store.lateSubscribe(it => callback(transform(it)))
