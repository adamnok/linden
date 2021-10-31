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

class HtmlGenerate[T](store: ReadStore[T]) {

  /**
   * @param content A html tartalmat ellőállító függvény.
   */
  def apply(content: Context ?=> T => Unit)(using context: Context, renderContext: RenderContext): Unit = {
    val (referenceID, clearReference) = renderContext.tree.createReferenceID()
    context.add(ReferenceElement(referenceID))
    val innerContext = new Context()
    store.subscribe { value =>
      innerContext.clear()
      content(using innerContext)(value)
      renderContext.tree.reRenderRequest(referenceID, new AbstractHtmlTree(innerContext, renderContext))
    }
    context.addCleared { () =>
      innerContext.clear()
      clearReference.apply()
    }
  }

  
  //inline private def getExplicitContext(using context: Context): Context = context
  
  /*def parameterLess(content: Context ?=> Unit)(using context: Context, renderContext: RenderContext): Unit = {
    apply(_ => content)
  }*/

  def partial(content: Context ?=> PartialFunction[T, Unit])(using context: Context, renderContext: RenderContext): Unit = {
    apply { value =>
      if (content.isDefinedAt(value)) {
        content.apply(value)//.apply(getExplicitContext) // todo testing
      }
    }
  }

}
