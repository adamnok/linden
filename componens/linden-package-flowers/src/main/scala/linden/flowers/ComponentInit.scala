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

package linden.flowers

import linden.component.ComponentFactory
import linden.di.DI
import linden.store.{ReadStore, Store}
import linden.util.dsl.*
import scala.language.experimental.macros
import linden.component.{Component as CoreComponent}

@Experimental
final class ComponentInitMethods[T <: Component] private[flowers]() {
  def apply(creator: DI => T): DIComponent = (session: DI) => creator.apply(session)

  //@Experimental
  //def resettable(creator: (DI, Resetting[T]) => T): DIComponent = { (session: DI) =>
  //  new ResettableComponent[T](session, resetting => creator.apply(session, resetting))
  //}

  @Experimental
  def resettable(creator: SelfResetting => DIComponent): DIComponent =
    (session: DI) => ResettableComponent(creator)

  @Experimental
  def resettableByStore(by: DI => ReadStore[Boolean])(creator: SelfResetting => DIComponent): DIComponent =
    (session: DI) => ResettableComponent(creator, Some(by(session)))

  @Experimental
  def withSession(updateSession: DI => DI, creator: DI => T): DIComponent =
    apply(creator compose updateSession)
}

@Experimental
trait SelfResetting:
  def apply(): Unit

@Experimental
trait Resetting extends SelfResetting:
  def apply(): Unit
  def apply(creator: Resetting => DIComponent): Unit

@Experimental
private final class ResettableComponent(
  creator: Resetting => DIComponent,
  by: Option[ReadStore[Boolean]] = None
) extends Component:

  object Never:
    def apply() = (it: Boolean) => !it
  private val resettingStore = Store(false -> creator)

  private val componentCreatorStore = resettingStore
    .flowControl(_._1)
    .map(_._2)
    .map(it => () => it(resetting))

  override def mounted(): Unit =
    by.foreach { byStore =>
      byStore.lateSubscribe { _ =>
        resettingStore.change(_.letLeft(Never()))
      }
    }

  private def resetting = new Resetting:
    override def apply(): Unit = resettingStore.change(_.letLeft(Never()))
    override def apply(creator: Resetting => DIComponent): Unit = resettingStore.change(it => Never()(it._1) -> creator)

  override lazy val render: Html = new Html:
    generate(componentCreatorStore)(+_ ())

end ResettableComponent

@Experimental
private final class SessionComponent[T <: Component](createSession: DI => DI, createComponent: ComponentFactory[DI]) extends Component {
  override lazy val render: Html = new Html {
    session(createSession) {
      +createComponent
    }
  }
}