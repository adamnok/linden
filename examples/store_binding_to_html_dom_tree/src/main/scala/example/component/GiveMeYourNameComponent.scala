package example.component

import linden.flowers.Component
import linden.store.Store

class GiveMeYourNameComponent extends Component:

  private val name = Store("")

  override def render = new Html:
    +"Give me your name please: "
    input(name)
    p {
      +name
    }
