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

package lindovo.internal

import minitest.*

object FieldsTypeFetcherSuite extends SimpleTestSuite:

  test("FieldsTypeFetcher.fromConstant function") {
    // Given
    val constant = Seq("String", "Int")

    // When
    val fetcher = FieldsTypeFetcher.fromConstant(constant)
    val columnsType = fetcher()

    // Then
    assertEquals(constant, columnsType)
  }

  test("FieldsTypeFetcher.byType function") {
    // Given
    case class Example(id: Int, name: String)

    // When
    val fetcher = FieldsTypeFetcher.byType[Example]
    val columnsType = fetcher()

    // Then
    val expectedColumnsType = Seq("scala.Int", "scala.Predef.String")
    assertEquals(columnsType, expectedColumnsType)
  }

  test("FieldsTypeFetcher.byType function with Option type") {
    // Given
    case class Example(id: Option[Int], name: Option[String])

    // When
    val fetcher = FieldsTypeFetcher.byType[Example]
    val columnsType = fetcher()

    // Then
    val expectedColumnsType = Seq("scala.Option[scala.Int]", "scala.Option[scala.Predef.String]")
    assertEquals(columnsType, expectedColumnsType)
  }