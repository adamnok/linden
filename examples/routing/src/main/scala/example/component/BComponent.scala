package example.component

import linden.flowers.Component
import linden.flowers.Component.factoryBy

object BComponent:
  def apply() = factoryBy[BComponent]

class BComponent extends Component:
  override def render = new Html:
    +"B Component"