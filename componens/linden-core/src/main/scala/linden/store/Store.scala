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

package linden.store

import linden.store.mutable.*

trait Store[I, O] extends ReadStore[O] with WriteStore[I]


object Store:
  def apply[T](current: T): SimpleStore[T, T] =
    SimpleStore.apply[T](current)

  def option[T]: SimpleStore[T, Option[T]] =
    SimpleStore.option[T]

  def fetch[T](current: T)(update: T => T): FetchStore[T] =
    FetchStore[T](current)(update)

  def fetchIncrement[I, T](index: I, current: T)(update: I => T)(using incrementer: Incrementer[I]): FetchIncrementStore[T] =
    FetchIncrementStore(index, current)(update)

  def forceUpdate() = ForceUpdateStore()

  def join[A, B](storeA: ReadStore[A], storeB: ReadStore[B]): ReadStore[(A, B)] =
    storeA join storeB

  def join[A, B, C](storeA: ReadStore[A], storeB: ReadStore[B], storeC: ReadStore[C]): ReadStore[(A, B, C)] =
    (storeA join storeB join storeC).map(it => (it._1._1, it._1._2, it._2))

  def join[A, B, C, D](storeA: ReadStore[A], storeB: ReadStore[B], storeC: ReadStore[C], storeD: ReadStore[D]): ReadStore[(A, B, C, D)] =
    (storeA join storeB join storeC join storeD).map(it => (it._1._1._1, it._1._1._2, it._1._2, it._2))

  def sequence[A](stores: Seq[ReadStore[A]]): ReadStore[Seq[A]] =
    if (stores.isEmpty)
      apply(Seq.empty[A])
    else
      stores.map(it => it.map(x => Seq[A](x)))
        .reduce { (a: ReadStore[Seq[A]], b: ReadStore[Seq[A]]) =>
          a.join(b).map(curr => curr._1 ++ curr._2)
        }
