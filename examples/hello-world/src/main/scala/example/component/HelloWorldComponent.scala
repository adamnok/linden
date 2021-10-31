package example.component

import linden.flowers.Component

class HelloWorldComponent() extends Component {
  override lazy val render = new Html {
    div {
      div {
        +"Hello, world!"
      }
    }
  }
}
