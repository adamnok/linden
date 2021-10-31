package todoapp.component

import linden.flowers.Component
import linden.flowers.Component.factoryBy
import linden.extension.bulma.BulmaHtml

import todoapp.statemanagement.TodoState


object ScoreUI:
  def apply() = factoryBy[ScoreUI]

class ScoreUI(state: TodoState) extends Component:

  override def render = new Html with BulmaHtml:
    level {
      levelItem("#ScoreForReady") {
        +state.score(_.ready.toString)
      }
      levelItem("#ScoreForUnReady") {
        +state.score(_.unReady.toString)
      }
    }
