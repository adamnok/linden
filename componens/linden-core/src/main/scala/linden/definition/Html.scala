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

import linden.component.{Component, ComponentFactory}
import linden.store.ReadStore

import scala.concurrent.Future
import scala.language.postfixOps

import linden.util.Experimental
import linden.experimental.ExperimentalExplicitContext
import linden.store.optimizing.IndexableOptimizing
import linden.store.optimizing.AppendableOptimizing
import linden.store.flow.OptimizingStore

open class Html[S]
  extends StandardHtmlElementsB
    with StandardInputHtmlElements
    with StandardSmartTypedInputHtmlElements
    with StandardRadioInputHtmlElements
    with StandardTextareaHtmlElements:
  implicit val renderContext: RenderContext = new RenderContext

  private def parseIdentity(value: String): List[Attribute] = {
    val (ids, classes, names) = value.split(" ")
      .map(_.trim)
      .filterNot(_.isEmpty)
      .map {
        case it if it.startsWith("#") =>
          (List(it), Nil, Nil)
        case it if it.startsWith("@") =>
          (Nil, Nil, List(it))
        case it =>
          (Nil, List(it), Nil)
      }
      .fold((Nil, Nil, Nil)) { case ((oId, oClazz, oName), (id, clazz, name)) =>
        (oId ++ id, oClazz ++ clazz, oName ++ name)
      }

    val joinedIds = ids
      .map(_ drop 1)
      .mkString(" ")
    val joinedClasses = classes
      .map(it => if (it(0) == '.') it drop 1 else it)
      .mkString(" ")
    val joinedNames = names
      .map(_ drop 1)
      .mkString(" ")
    List(
      StaticAttribute("id", joinedIds),
      StaticAttribute("class", joinedClasses),
      StaticAttribute("name", joinedNames)
    )
      .filterNot(_.value.isEmpty)
  }

  override def element(name: String, content: => Unit, context: Context): Unit = {
    context.begin(name)
    content
    context.end()
  }

  override def element(name: String, identity: String, content: => Unit, context: Context): Unit = {
    context.begin(name)
    parseIdentity(identity).foreach(context add)
    content
    context.end()
  }

  override def elementWith(
    name: String,
    identity: String,
    contentInner: (HtmlAttributes, HtmlEvents) => Any,
    contentOut: => Unit
  )(using context: Context): Unit = {
    context.begin(name)
    parseIdentity(identity).foreach(context add)
    contentInner(new HtmlAttributes, new HtmlEvents)
    contentOut
    context.end()
  }


  def o(using context: Context, renderContext: RenderContext) = new HtmlAttributes

  def e(using context: Context) = new HtmlEvents

  def generate[T](store: ReadStore[T]): HtmlGenerate[T] = new HtmlGenerate[T](store)

  def generate[T](store: OptimizingStore[Seq[T], Seq[IndexableOptimizing.Action[T]]]): HtmlGenerateOpimizeByIndex[T] = new HtmlGenerateOpimizeByIndex[T](store)

  def generate[T](store: OptimizingStore[Seq[T], Seq[AppendableOptimizing.Action[T]]]): HtmlGenerateOptimizeByAppend[T] = new HtmlGenerateOptimizeByAppend[T](store)

  def generate[T](store: ReadStore[Future[T]]): HtmlGenerateFromFuture[T] = new HtmlGenerateFromFuture[T](store)

  def session(session: S => S): HtmlSessionContext[S] = new HtmlSessionContext(session)

  def css = new HtmlCss

  def shadow = new HtmlShadow

  extension (text: String)(using context: Context)
    def unary_+ : Unit = context add TextElement(text)

  extension (textStore: ReadStore[String])(using context: Context)
    def unary_+ : Unit =
      generate(textStore) { text =>
          +text
      }

  extension [S](component: Component[S])(using context: Context)
    def unary_+ : Unit =
      context add ComponentElement(ComponentFactory.preDefined(component))

  extension [S] (factory: ComponentFactory[S])(using context: Context)
    def unary_+ : Unit =
      context add ComponentElement(factory)

  /**
   * @since Linden Core 1.2.1 with experimental flag
   */
  @Experimental
  inline def explicitContext(using context: Context)(using ExperimentalExplicitContext): Context = context
