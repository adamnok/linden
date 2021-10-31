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
import linden.store.optimizing.AppendableOptimizing
import linden.store.flow.OptimizingStore
import linden.util.dsl.*
import linden.definition.RenderContextForAppendableAbstractHtmlTree.{AppendToBottomRenderRequest, AppendToTopRenderRequest, FullReRenderRequest}

class HtmlGenerateOptimizeByAppend[T](store: OptimizingStore[Seq[T], Seq[AppendableOptimizing.Action[T]]]):
  type Item = T

  def apply(content: Context ?=> T => Unit)(
    using context: Context, renderContext: RenderContext
  ): Unit =
    appendable(store, content)

  private def appendable(
    source: OptimizingStore[Seq[Item], Seq[AppendableOptimizing.Action[Item]]],
    content: Context ?=> Item => Unit
  )(
    using context: Context, renderContext: RenderContext
  ): Unit =
    val (referenceID, clearReference) = renderContext.appendableTree.createReferenceID()
    context.add(ReferenceAppendableElement(referenceID))

    var innerContexts = Seq.empty[Context]
    val lock = ""
    source.subscribe {
      case OptimizingStore.Init(init) =>
        val innerContext = new Context()
        init.foreach { item =>
          content(using innerContext)(item)
        }
        renderContext.appendableTree.reRenderRequest(
          referenceID = referenceID,
          action = FullReRenderRequest(new AbstractHtmlTree(innerContext, renderContext))
        )
        lock.synchronized {
          innerContexts = Seq(innerContext)
        }

      case OptimizingStore.Update(actions) =>
        actions.foreach {
          case AppendableOptimizing.Action.Clear =>
            val innerContext = new Context()
            renderContext.appendableTree.reRenderRequest(
              referenceID = referenceID,
              action = FullReRenderRequest(new AbstractHtmlTree(innerContext, renderContext))
            )
            lock.synchronized {
              innerContexts.foreach(_.clear())
              innerContexts = Seq(innerContext)
            }
          case AppendableOptimizing.Action.ToTop(items) =>
            val innerContext = new Context()
            items.foreach { item =>
              content(using innerContext)(item)
            }
            renderContext.appendableTree.reRenderRequest(
              referenceID = referenceID,
              action = AppendToTopRenderRequest(new AbstractHtmlTree(innerContext, renderContext))
            )
            lock.synchronized {
              innerContexts = innerContexts ++ Seq(innerContext)
            }
          case AppendableOptimizing.Action.ToBottom(items) =>
            val innerContext = new Context()
            items.foreach { item =>
              content(using innerContext)(item)
            }
            renderContext.appendableTree.reRenderRequest(
              referenceID = referenceID,
              action = AppendToBottomRenderRequest(new AbstractHtmlTree(innerContext, renderContext))
            )
            lock.synchronized {
              innerContexts = innerContexts ++ Seq(innerContext)
            }
        }
    }
    context.addCleared { () =>
      lock.synchronized {
        innerContexts.foreach(_.clear())
        innerContexts = Seq()
      }
      clearReference.apply()
    }
  end appendable
