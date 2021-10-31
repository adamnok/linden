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

package linden.renderer.browser.render

import linden.component.Component
import linden.definition.*
import linden.renderer.browser.render.renderable.{
  Renderable,
  RenderableCssElement,
  RenderableHtmlElement,
  RenderableShadowHtmlElement
}
import linden.renderer.browser.util.LoggerFactory as BrowserLoggerFactory
import linden.util.dsl.*
import linden.util.{Logger, LoggerFactory}
import org.scalajs.dom
import linden.renderer.browser.render.renderable

object DOMRenderer {
  def apply[S](container: dom.Element, session: S, headComponent: Component[S]): Unit = {
    apply[S](
      loggerFactory = new BrowserLoggerFactory(),
      container = container,
      session = session,
      headComponent = headComponent
    )
  }

  def apply[S](loggerFactory: LoggerFactory, container: dom.Element, session: S, headComponent: Component[S]): Unit = {
    apply[S](loggerFactory, container, session, headComponent.abstractHtmlTree)
    headComponent.mounted()
  }

  private def apply[S](loggerFactory: LoggerFactory, container: dom.Element, session: S, aht: AbstractHtmlTree): Unit = {
    val (children, after, dis) = new DOMRenderer(loggerFactory, session, aht).render()
    val item = new renderable.RenderableFixed(container)
    item += children
    after.foreach(_ (item))
  }
}

private class DOMRenderer[S](
  val loggerFactory: LoggerFactory,
  initSession: S,
  aht: AbstractHtmlTree
) extends DOMAttributeRender :

  override val logger: Logger = loggerFactory.create("DOMRenderer")

  type Rendered = (List[renderable.Renderable], Option[renderable.Renderable => Unit], Option[() => Unit])

  private val tree: List[Element] = aht.tree
  override val renderContext: RenderContext = aht.renderContext

  def render(): Rendered = tree.map(render(initSession))
    .unzip3
    .letFirst(_.flatten)
    .letSecond(it =>
      Some { (parent: renderable.Renderable) =>
        it.foreach(_.foreach(_ (parent)))
      }
    )
    .letThird(child =>
      child.filter(_.isDefined)
        .let(it => if (it.isEmpty) None else Some(() => it.flatten.foreach(_.apply())))
    )

  private def newRender(loggerFactory: LoggerFactory, session: S, ahts: Seq[AbstractHtmlTree]): Rendered = {
    ahts.map(new DOMRenderer(loggerFactory, session, _).render())
      .let(_.unzip3)
      .letFirst { newChildren => newChildren.flatten.toList }
      .letSecond {
        _
          .filter(_.isDefined)
          .map(_.get)
          .let {
            case Seq() => None
            case afterInvokes => Some {
              (parent: renderable.Renderable) => afterInvokes.foreach(_ (parent))
            }
          }
      }
      .letThird {
        _
          .filter(_.isDefined)
          .map(_.get)
          .let {
            case Seq() => None
            case earlyDids => Some {
              () => earlyDids.foreach(_ ())
            }
          }
      }
  }

  /**
    * Adott elem renderelésésnek elvégzése.
    */
  private[render] def render(session: S)(element: Element): Rendered =
    element match
      case block: BlockElement =>
        val renderable = new RenderableHtmlElement(block.name)
        attributes(renderable, block.attributes)
        block.events.foreach(event => renderable.addEvent(event.name, event.callback))
        val (children, afterInvokes, did) = block.children
          .map(render(session))
          .unzip3
          .letFirst(_.flatten)
          .letThird(did =>
            did.filter(_.isDefined)
              .let(it => if (it.isEmpty) None else Some(() => it.flatten.foreach(_ ())))
          )
        renderable += children
        afterInvokes.foreach(_.foreach(_ (renderable)))
        (List(renderable), None, did)

      case TextElement(value) =>
        (List(new renderable.RenderableTextHtmlElement(value)), None, None)

      case ReferenceElement(referenceID) =>
        val referencedTree = renderContext.tree(referenceID)
        val lock = new Object()
        val (initRendered, afterInvoke, earlyDid) = new DOMRenderer(loggerFactory, session, referencedTree).render()
        var current = initRendered -> earlyDid
        val separator = new renderable.RenderableHtmlElement("span")
        val resultRenderable = initRendered :+ separator
        val resultAfterInvoke = Some {
          (parent: renderable.Renderable) =>
            afterInvoke.foreach(_ (parent))
            renderContext.tree.addObserver(referenceID) {
              tree =>
                lock.synchronized {
                  parent -= current._1
                  current._2.foreach(_ ())
                  val (newChildren, afterInvoke, did) = new DOMRenderer(loggerFactory, session, tree).render()
                  newChildren.foreach(parent.insertBeforeChild(_, separator))
                  afterInvoke.foreach(_ (parent))
                  current = newChildren -> did
                }
            }
        }
        val resultDid = Some(() => current._2.foreach(_ ()))
        (resultRenderable, resultAfterInvoke, resultDid)

      case ReferenceAppendableElement(referenceID) =>
        import linden.definition.RenderContextForAppendableAbstractHtmlTree.*
        val referencedTree = renderContext.appendableTree(referenceID)
        val lock = new Object()
        val (initRendered, afterInvoke, earlyDid) = newRender(loggerFactory, session, referencedTree)
        var current = initRendered -> earlyDid
        val topSeparator = new renderable.RenderableHtmlElement("span")
        val bottomSeparator = new renderable.RenderableHtmlElement("span")
        val resultRenderable = topSeparator +: initRendered :+ bottomSeparator
        val resultAfterInvoke = Some {
          (parent: renderable.Renderable) =>
            afterInvoke.foreach(_ (parent))
            renderContext.appendableTree.addObserver(referenceID) {
              case FullReRenderRequest(trees) =>
                lock.synchronized {
                  parent -= current._1
                  current._2.foreach(_ ())
                  val (newChildren, afterInvoke, did) = newRender(loggerFactory, session, trees)
                  newChildren.foreach(parent.insertBeforeChild(_, bottomSeparator))
                  afterInvoke.foreach(_ (parent))
                  current = newChildren -> did
                }
              case reRenderRequest: ReRenderRequest =>
                lock.synchronized {
                  val (beforeChildren, beforeDid) = current
                  val (newChildren, afterInvoke, did) = newRender(loggerFactory, session, reRenderRequest.value)
                  reRenderRequest match {
                    case AppendToTopRenderRequest(_) =>
                      newChildren.foreach(parent.insertBeforeChild(_, topSeparator.native.nextSibling)) // TODO ??? beforeChildren.head.native
                    case AppendToBottomRenderRequest(_) =>
                      newChildren.foreach(parent.insertBeforeChild(_, bottomSeparator))
                    case FullReRenderRequest(_) =>
                      throw new IllegalStateException
                  }
                  afterInvoke.foreach(_ (parent))
                  val newDid = Some { () =>
                    beforeDid.foreach(_ ())
                    did.foreach(_ ())
                  }
                  current = (beforeChildren ++ newChildren) -> newDid
                }
            }
        }
        val resultDid = Some(() => current._2.foreach(_ ()))
        (resultRenderable, resultAfterInvoke, resultDid)

      case ReferenceIndexableElement(referenceID) =>
        import linden.definition.RenderContextForIndexableAbstractHtmlTree.*
        val referencedTree = renderContext.indexableTree(referenceID)
        val lock = new Object()
        val (initRendered, afterInvoke, earlyDid) = referencedTree.map(new DOMRenderer(loggerFactory, session, _).render()).unzip3.letFirst(_.toList)
        var current = initRendered -> earlyDid
        val topSeparator = new renderable.RenderableHtmlElement("span")
        val bottomSeparator = new renderable.RenderableHtmlElement("span")
        val resultRenderable = topSeparator +: initRendered.flatten :+ bottomSeparator
        val resultAfterInvoke = Some {
          (parent: renderable.Renderable) =>
            afterInvoke.foreach(_.foreach(_ (parent)))
            renderContext.indexableTree.addObserver(referenceID) { actions =>
              lock.synchronized {
                val (beforeChildren, beforeDid) = current

                if (actions.length == 1 && actions.head == Clear) {
                  (beforeChildren zip beforeDid).foreach {
                    case (child, did) =>
                      child.foreach(parent -= _)
                      did.foreach(_ ())
                  }
                  current = List() -> List()
                } else {
                  val (newChildren, newDid) =
                    actions.foldLeft(
                      topSeparator.asInstanceOf[Renderable],
                      (List.empty[(List[renderable.Renderable], Option[() => Unit])] -> (beforeChildren zip beforeDid)),
                      Map.empty[Int, (List[Renderable], Option[() => Unit])]
                    ) {
                      case (_, Init(_)) => throw new IllegalArgumentException
                      case ((idc, (acc, xs), m), Insert(n_aht)) =>
                        val (newChildren, afterInvoke, earlyDid) = new DOMRenderer(loggerFactory, session, n_aht).render()
                        val nextIDC =
                          xs.headOption.flatMap(_._1.headOption).map(_.native).getOrElse(idc.native.nextSibling)
                        newChildren.foreach(parent.insertBeforeChild(_, nextIDC))
                        afterInvoke.foreach(_ (parent))
                        (newChildren.last, ((acc :+ (newChildren -> earlyDid)) -> xs), m)
                      case ((idc, (acc, (it@(child, _)) :: xs), m), Stay) =>
                        (child.lastOption.getOrElse(idc), ((acc :+ it) -> xs), m)
                      case ((idc, (acc, (child, did) :: xs), m), Remove) =>
                        child.foreach(parent -= _)
                        did.foreach(_ ())
                        (idc, (acc -> xs), m)
                      case ((idc, (acc, (it@(child, _)) :: xs), m), Save(id)) =>
                        child.foreach(parent -= _)
                        (idc, (acc -> xs), m + (id -> it))
                      case ((idc, (acc, xs), m), Load(id)) =>
                        val (newChildren, earlyDid) = m(id)
                        val nextIDC =
                          xs.headOption.flatMap(_._1.headOption).map(_.native).getOrElse(idc.native.nextSibling)
                        newChildren.foreach(parent.insertBeforeChild(_, nextIDC))
                        (newChildren.last, ((acc :+ (newChildren -> earlyDid)) -> xs), m - id)
                      case _ =>
                        throw new IllegalStateException
                    }._2._1.unzip
                  current = newChildren -> newDid
                }
              }
            }
        }
        val resultDid = Some(() => current._2.foreach(_.foreach(_ ())))
        (resultRenderable, resultAfterInvoke, resultDid)

      case ComponentElement(factory) =>
        val component = factory.create(session.asInstanceOf)
        val aht = component.abstractHtmlTree
        new DOMRenderer(loggerFactory, session, aht).render()
          .letSecond(it =>
            Some { (parent: renderable.Renderable) =>
              it.foreach(_ (parent))
              component.mounted()
            }
          )
          .letThird(childUnmounted =>
            Some { () =>
              childUnmounted.foreach(_.apply)
              component.unmounted()
              component.context.clear()
            }
          )

      case SessionInjector(sessionInjection, children) =>
        val newSession = sessionInjection.asInstanceOf[S => S](session)
        val aht = children
        new DOMRenderer(loggerFactory, newSession, aht).render()
          .letSecond(it =>
            Some {
              (parent: renderable.Renderable) =>
                it.foreach(_ (parent))
            }
          )
          .letThird(childUnmounted =>
            Some(() => childUnmounted.foreach(_.apply))
          )

      case CssElement(value) =>
        println("Css element render")
        val renderable = new RenderableCssElement(value)
        (List(renderable), None, None)

      case ShadowElement(shadowElement: BlockElement) =>
        val renderable = new RenderableShadowHtmlElement(shadowElement.name)
        attributes(renderable, shadowElement.attributes)
        shadowElement.events.foreach(event => renderable.addEvent(event.name, event.callback))
        val (children, afterInvokes, did) = shadowElement.children
          .map(render(session))
          .unzip3
          .letFirst(_.flatten)
          .letThird(did =>
            did.filter(_.isDefined)
              .let(it => if (it.isEmpty) None else Some(() => it.flatten.foreach(_ ())))
          )
        renderable += children
        afterInvokes.foreach(_.foreach(_ (renderable)))
        (List(renderable), None, did)
