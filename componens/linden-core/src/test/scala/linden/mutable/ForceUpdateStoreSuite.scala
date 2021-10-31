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

package linden.store.mutable

import minitest.*
import linden.store.Store
import linden.store.ClearContext
import linden.store.mutable.ForceUpdateStore.NeverEQ

object ForceUpdateStoreSuite extends SimpleTestSuite:
  test("#1") {
    val store = ForceUpdateStore()
    var invoked = false

    given ClearContext() with
      def addCleared(callback: () => Unit): Unit = {}

    store.lateSubscribe { _ =>
      invoked = true
    }
    
    store.forceUpdate()
    assert(invoked)
  }
  test("#2") {
    given ClearContext() with
      def addCleared(callback: () => Unit): Unit = {}

    val a = Store(0)
    val store = ForceUpdateStore()
    val force = store.zipWithLastValue(NeverEQ).map(_ ne _)
      
    var invokeds = Seq.empty[(Int, Boolean)]

    (a zip force).lateSubscribe { it =>
      invokeds = invokeds :+ it
    }
    
    a.change(1)
    a.change(2)
    a.change(3)
    println("AA")
    store.forceUpdate()
    store.forceUpdate()

    assertEquals(invokeds, Seq((1,true), (2,false), (3,false), (3,true), (3,true)))
  }