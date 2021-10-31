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

package linden.di

import minitest.*

object DIGraphSuite extends SimpleTestSuite:
  test("getting through graph") {
    // Given
    val diA = new DI:
      ref[String] singleton "Alma"

    val diB = new DIGraph(diA):
      ref[String] singleton "Endraw"

    val diC = new DIGraph(diA){}

    val diE = new DIGraph(diB){}

    val diF = new DIGraph(diB){}

    // When
    val result = diC[String]
    
    // Then
    assertEquals(result, "Alma")

    // When
    val result2 = diE[String]

    // Then
    assertEquals(result2, "Endraw")
  }
  test("use value through graph") {
    case class A(name: String)
    case class B(name: String)
    // Given
    val diA = new DI:
      ref[String] singleton "Alma"
      ref[A] factory createInstance[A]
      ref[B] factory createInstance[B]

    val diB = new DIGraph(diA):
      ref[String] singleton "Endraw"
      ref[A] factory createInstance[A]

    // Whens + Thens
    assertEquals(diA[A].name, "Alma")
    assertEquals(diA[B].name, "Alma")
    assertEquals(diB[A].name, "Endraw")
    assertEquals(diB[B].name, "Alma")
  }