package example.functionalstore.client.component

import example.functionalstore.client.state.HeroState
import linden.flowers.Component
import linden.flowers.Component.factoryBy


object ShowHeroComponent:
  def apply() = factoryBy[ShowHeroComponent]

class ShowHeroComponent(store: HeroState) extends Component:
  override lazy val render = new Html:
    div {
      +"Selected hero: "
      generate(store.name) {
        case Some(name) => +name
        case None => +"not selected"
      }
      /*
       * Other solution is
       *    ```
       *      generate(store.name) { name =>
       *        +name.getOrElse("not selected")
       *      }
       *    ```
       * or
       *    ``` +store.name.map(_ getOrElse "not selected") ```
       * or
       *    ``` +store.name(_ getOrElse "not selected") ```
       */
    }
