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

package linden.definition

import linden.store.ReadStore

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HtmlGenerateFromFuture[T](store: ReadStore[Future[T]]) {

  /**
    * @param content A html tartalmat ellőállító függvény.
    */
  def apply(content: Context ?=> T => Unit)(implicit context: Context,
    renderContext: RenderContext, executionContext: ExecutionContext
  ): Unit = {
    val (referenceID, clearReference) = renderContext.tree.createReferenceID()
    context.add(ReferenceElement(referenceID))
    val innerContext = new Context()
    renderContext.tree.reRenderRequest(referenceID, new AbstractHtmlTree(innerContext, renderContext))
    store.subscribe { futureValue =>
      futureValue.onComplete {
        case Success(value) =>
          innerContext.clear()
          content(using innerContext)(value)
          renderContext.tree.reRenderRequest(referenceID, new AbstractHtmlTree(innerContext, renderContext))
        case Failure(_) =>
          // TODO
      }
    }
    context.addCleared { () =>
      innerContext.clear()
      //clearStoreSubScribe.apply()
      clearReference.apply()
    }
  }

}
