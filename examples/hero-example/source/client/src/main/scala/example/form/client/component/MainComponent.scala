package example.form.client.component

import linden.flowers.Component

class MainComponent() extends Component:

  override lazy val render: Html = new Html:
    div {
      div {
        +HeroExampleInput()
      }
    }
