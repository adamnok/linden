package example.component

import linden.flowers.Component
import linden.flowers.Component.factoryBy

object AComponent:
  def apply() = factoryBy[AComponent]

class AComponent extends Component:
  override def render = new Html:
    +"A Component"