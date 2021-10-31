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

import linden.store.ClearContext
import linden.store.functional.Real
import linden.store.mutable.SimpleStore

class WrapStore[I, O, WI, WO](store: SimpleStore[I, O], forRead: O => WO, forWrite: WI => I) extends SimpleStore[WI, WO]:
  override def change(value: WI): Unit = store.change(forWrite(value))

  override def change(value: WO => WI): Unit = store.change(forWrite compose value compose forRead)

  override def value: WO = forRead(store.value)

  override def subscribe(callback: WO => Unit)(using context: ClearContext): Unit =
    store.subscribe(callback compose forRead)

  override def lateSubscribe(callback: WO => Unit)(using context: ClearContext): Unit =
    store.lateSubscribe(callback compose forRead)

  override def wrapper[T](forRead: WO => T, forWrite: T => WI): SimpleStore[T, T] =
    WrapStore[WI, WO, T, T](this, forRead, forWrite)
