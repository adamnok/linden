package example.component

import linden.flowers.Component
import linden.flowers.Component.factoryBy

object CComponent:
  def apply() = factoryBy[CComponent]


class CComponent() extends Component:
  override def render = new Html:
    +"C Component"
