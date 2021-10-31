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

package linden.di

import scala.collection.mutable

object DI:
  // TODO: can by private
  val defaultName = "#"
  
trait DIExtension:
  extension [T](reference: Reference[T])
    def apply()(using di: DI): BindedDI[T] = di.bind[T](reference)

    def apply(name: String)(using di: DI): BindedDI[T] = di.bind[T](reference, name)

    infix def bind()(using di: DI): BindedDI[T] = di.bind[T](reference)

    infix def bind(name: String)(using di: DI): BindedDI[T] = di.bind[T](reference, name)

    infix def factory(item: => T)(using di: DI): Unit = apply().factory(item)

    infix def singleton(item: => T)(using di: DI): Unit = apply().singleton(item)

open class DI extends SessionDI with DIExtension:

  protected given di: DI = this

  private val map = mutable.HashMap[String, mutable.HashMap[String, () => Any]]()

  private[di] def use(di: DI): Unit =
    di.map.toList.foreach { case (key, elements) =>
      map += key -> mutable.HashMap.from(elements)
    }

  protected def use[T <: DIPlugin](di: T): Unit = use(di.apply)

  inline def apply[T]: T = apply(linden.di.DIMacros.ref[T])

  def apply[T](reference: Reference[T]): T =
    apply(reference.canonicalClassName, DI.defaultName)

  def apply[T](reference: Reference[T], name: String): T =
    apply(reference.canonicalClassName, name)

  def apply[T](clazzType: String, name: String): T =
    get[T](clazzType, name)
      .getOrElse(throw new IllegalStateException(s"Not found: clazzType=$clazzType with name=$name"))

  inline def get[T]: Option[T] = get(linden.di.DIMacros.ref[T])

  def get[T](reference: Reference[T]): Option[T] =
    get[T](reference.canonicalClassName, DI.defaultName)

  def get[T](reference: Reference[T], name: String): Option[T] =
    get[T](reference.canonicalClassName, name)

  def get[T](clazzType: String, name: String): Option[T] =
    map.get(clazzType)
      // Stream -> LazyList
      .flatMap(it => LazyList(name, DI.defaultName).map(it.get).find(_.isDefined))
      .flatten
      .map(_.apply())
      .map(_.asInstanceOf[T])

  private def put(clazzType: String, ref: () => Any): Unit =
    put(clazzType, DI.defaultName, ref)

  private def put(clazzType: String, name: String, ref: () => Any): Unit =
    val inMap = map.getOrElse(clazzType, {
      val inMap = mutable.HashMap[String, () => Any]()
      map.put(clazzType, inMap)
      inMap
    })
    inMap.put(name, ref)

  def bind[T](reference: Reference[T]): BindedDI[T] = bind(reference.canonicalClassName, DI.defaultName)

  def bind[T](reference: Reference[T], name: String): BindedDI[T] = bind(reference.canonicalClassName, name)

  def bind[T](clazzType: String, name: String): BindedDI[T] =
    new BindedDI[T](clazzType, name)

  inline def createInstance[T]: T =
    linden.di.DIMacros.createInstanceReverseArgs[T](this)

  inline def createInstance[T](inline args: Any*): T =
    linden.di.DIMacros.createInstanceReverseArgs[T](this)(args*)

  inline def createInstance[T](inline args1: Any*)(inline args2: Any*): T =
    linden.di.DIMacros.createInstanceReverseArgs[T](this)(args1*)(args2*)
  
  inline def createInstance[T](inline args1: Any*)(inline args2: Any*)(inline args3: Any*): T =
    linden.di.DIMacros.createInstanceReverseArgs[T](this)(args1*)(args2*)(args3*)

  inline protected def ref[T]: Reference[T] =
    linden.di.DIMacros.ref[T]

  protected class BindedDI[T](clazzType: String, name: String) extends linden.di.BindedDI[T]:
    infix def singleton(item: => T): Unit =
      lazy val singletonItem: T = item
      put(clazzType, name, () => singletonItem)
    infix def factory(item: => T): Unit =
      put(clazzType, name, () => item)
  end BindedDI
end DI
