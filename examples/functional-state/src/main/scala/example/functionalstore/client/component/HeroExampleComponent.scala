package example.functionalstore.client.component

import example.functionalstore.client.state.HeroState
import linden.flowers.Component
import linden.flowers.Component.factoryBy
import linden.store.Store

object HeroExampleComponent:
  def apply() = factoryBy[HeroExampleComponent]

class HeroExampleComponent(store: HeroState) extends Component:

  private val nameStore = Store("")

  override def mounted(): Unit =
    nameStore
      .filter(it => it.length >= 3)
      .subscribe { name =>
        store.actions.fillWithHero(name)
      }

  override lazy val render = new Html:
    div {
      generate(store.name) {
        case None =>
          +"What is name of your hero?"
          input(nameStore)
        case _ =>
      }
    }
