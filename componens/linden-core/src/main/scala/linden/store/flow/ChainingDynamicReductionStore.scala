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

import linden.store.{ReadStore, ClearContext, Store}

/**
 *
 * @tparam T Az eredeti store típusa.
 *
 * @param store A mappelendő store
 */
private[linden] class ChainingDynamicReductionStore[T](
  private val store: ReadStore[T]
) extends ReadStore[T]:
  
  private val current = Store[T](store.value)

  override def value: T = current.value

  override def subscribe(callback: T => Unit)(using context: ClearContext): Unit =
    addReference(callback)
    current.subscribe(it => callback(it))

  override def lateSubscribe(callback: T => Unit)(using context: ClearContext): Unit =
    addReference(callback)
    current.lateSubscribe(it => callback(it))

  private var references = Map[T => Unit, ClearContext]()
  private var currentReference: Option[T => Unit] = None

  private def addReference(callback: T => Unit)(using context: ClearContext): Unit =
    context.addCleared { () =>
      this.synchronized {
        references -= callback
        currentReference
          .filter(_ == callback)
          .foreach { currentReference =>
            references.headOption.foreach { case (callback, clearContext) =>
              createSubscribe(callback)(using clearContext)
            }
          }
      }
    }
    this.synchronized {
      references += callback -> context
      if (currentReference.isEmpty) {
        createSubscribe(callback)
      }
    }

  private def createSubscribe(callback: T => Unit)(using context: ClearContext): Unit =
    this.synchronized {
      currentReference = Some(callback)
      store.subscribe(current.change)
    }
