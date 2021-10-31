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

package linden.util

package object dsl {

  extension [T] (data: T)
    def also(callback: T => Unit): T =
      callback(data)
      data

    def let[G](callback: T => G): G =
      callback(data)

  extension [L, R](data: (L, R))
    def letLeft[G](callback: L => G): (G, R) = callback(data._1) -> data._2

    def letRight[G](callback: R => G): (L, G) = data._1 -> callback(data._2)

  extension [A, B, C](data: (A, B, C))
    def letFirst[G](callback: A => G): (G, B, C) = data.copy(_1 = callback(data._1))

    def letSecond[G](callback: B => G): (A, G, C) = data.copy(_2 = callback(data._2))

    def letThird[G](callback: C => G): (A, B, G) = data.copy(_3 = callback(data._3))

  extension [T](seq: Seq[T])
    def takeLastWhile(predicate: T => Boolean): Seq[T] = seq.reverse.takeWhile(predicate).reverse

}
