package example.component

import linden.flowers.Component
import linden.flowers.Component.factoryBy

import example.Data

object AComponent:
  def apply() = factoryBy[AComponent]

class AComponent(data: Data) extends Component:
  override def render = new Html:
    +data.value