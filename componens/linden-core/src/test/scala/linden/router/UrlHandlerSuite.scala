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

package linden.router

import minitest.*
import linden.store.Store
import linden.store.ClearContext

object UrlHandlerSuite extends SimpleTestSuite:
  test("#1") {
    // Given
    val url = "/a"
    val handler = UrlHandler(url)
    val pattern = "/a"

    // When
    val result = handler.isPattern(pattern)

    // Then
    assert(result)
  }
  test("#2") {
    // Given
    val url = "/a"
    val handler = UrlHandler(url)
    val pattern = "/b"

    // When
    val result = handler.isPattern(pattern)

    // Then
    assert(!result)
  }
  test("#3") {
    // Given
    val url = "/a"
    val handler = UrlHandler(url)
    val pattern = "/b"

    // When
    val result = handler.hashPattern(pattern)

    // Then
    assertEquals(result, None)
  }