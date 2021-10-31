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
  * Store értékének szűrése.
  *
  * @tparam T Az store típusa.
  * @param store  A mappelendő store
  * @param filter Szűrési feltétel
  */
class FilteredStore[T](
  private val store: ReadStore[T],
  private val filter: T => Boolean
) extends ReadStore[T]:

  override def value: T = store.value

  override def subscribe(callback: T => Unit)(using context: ClearContext): Unit =
    store.subscribe(it => if (filter(it)) callback(it))

  override def lateSubscribe(callback: T => Unit)(using context: ClearContext): Unit =
    store.lateSubscribe(it => if (filter(it)) callback(it))
