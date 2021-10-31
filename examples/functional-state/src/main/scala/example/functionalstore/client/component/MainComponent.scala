package example.functionalstore.client.component

import linden.flowers.Component

class MainComponent() extends Component:
  override lazy val render = new Html:
    div {
      +HeroExampleComponent()
      +ShowHeroComponent()
    }
