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
import linden.store.optimizing.IndexableOptimizing
import linden.store.optimizing.IndexableOptimizing
import linden.store.flow.OptimizingStore
import linden.util.dsl.*

class HtmlGenerateOpimizeByIndex[T](store: OptimizingStore[Seq[T], Seq[IndexableOptimizing.Action[T]]]):
  type Item = T

  def apply(content: Context ?=> T => Unit)(
    using context: Context, renderContext: RenderContext
  ): Unit =
    indexable(store, content)


  private def indexable(
    source: OptimizingStore[Seq[Item], Seq[IndexableOptimizing.Action[Item]]],
    content: Context ?=> Item => Unit
  )(
    using context: Context, renderContext: RenderContext
  ): Unit =
    import linden.definition.RenderContextForIndexableAbstractHtmlTree.*
    val (referenceID, clearReference) = renderContext.indexableTree.createReferenceID()
    context.add(ReferenceIndexableElement(referenceID))

    var innerContexts = Seq.empty[Context]
    val lock = ""
    source.subscribe {
      case OptimizingStore.Init(values) =>
        val newInnerContexts = values
          .map { item =>
            new Context().also(content(using _)(item))
          }
        renderContext.indexableTree.reRenderRequest(
          referenceID = referenceID,
          action = Init(newInnerContexts.map(new AbstractHtmlTree(_, renderContext)))
        )
        lock.synchronized {
          innerContexts = newInnerContexts
        }

      case OptimizingStore.Update(Seq(IndexableOptimizing.Action.Clear)) =>
        val newInnerContext = new Context()
        renderContext.indexableTree.reRenderRequest(
          referenceID = referenceID,
          action = Clear
        )
        lock.synchronized {
          innerContexts.foreach(_.clear())
          innerContexts = Seq(newInnerContext)
        }

      case OptimizingStore.Update(actions) =>
        case class Exec(
          newInnerContexts: Seq[Context] = Seq(),
          newActions: Seq[LateAction] = Seq(),
          clearableInnerContexts: Seq[Context] = Seq(),
          currentInnerContexts: Seq[Context],
          store: Map[Int, Context] = Map()
        )
        val exec = actions.foldLeft(Exec(currentInnerContexts = innerContexts)) {
          case (exec@Exec(acc_context, acc_action, _, _, _), IndexableOptimizing.Action.Insert(item)) =>
            val innerContext = new Context().also(it => content(using it)(item))
            val new_acc_context = acc_context :+ innerContext
            val new_acc_action = acc_action :+ Insert(new AbstractHtmlTree(innerContext, renderContext))
            exec.copy(
              newInnerContexts = new_acc_context,
              newActions = new_acc_action
            )
          case (exec@Exec(acc_context, acc_action, _, it :: xs, _), IndexableOptimizing.Action.Stay) =>
            val new_acc_context = acc_context :+ it
            val new_acc_action = acc_action :+ Stay
            exec.copy(
              newInnerContexts = new_acc_context,
              newActions = new_acc_action,
              currentInnerContexts = xs
            )
          case (exec@Exec(acc_context, acc_action, acc_clearable, it :: xs,_ ), IndexableOptimizing.Action.Remove) =>
            val new_acc_context = acc_context
            val new_acc_action = acc_action :+ Remove
            val new_acc_clearable = acc_clearable :+ it
            exec.copy(
              newInnerContexts = new_acc_context,
              newActions = new_acc_action,
              clearableInnerContexts = new_acc_clearable,
              currentInnerContexts = xs
            )
          case (exec@Exec(_, acc_action, _, it :: xs, acc_store), IndexableOptimizing.Action.Save(id)) =>
            val new_acc_action = acc_action :+ Save(id)
            val new_acc_store = acc_store + (id -> it)
            exec.copy(
              newActions = new_acc_action,
              currentInnerContexts = xs,
              store = new_acc_store
            )
          case (exec@Exec(acc_context, acc_action, _, _, acc_store), IndexableOptimizing.Action.Load(id)) =>
            val new_acc_context = acc_context :+ acc_store(id)
            val new_acc_action = acc_action :+ Load(id)
            val new_acc_store = acc_store - id
            exec.copy(
              newInnerContexts = new_acc_context,
              newActions = new_acc_action,
              store = new_acc_store
            )
          case _ =>
            throw new IllegalStateException
        }
        renderContext.indexableTree.reRenderRequest(
          referenceID = referenceID,
          action = exec.newActions
        )
        lock.synchronized {
          exec.clearableInnerContexts.foreach(_.clear())
          innerContexts = exec.newInnerContexts
        }
    }

    context.addCleared { () =>
      lock.synchronized {
        innerContexts.foreach(_.clear())
        innerContexts = Seq()
      }
      clearReference.apply()
    }
