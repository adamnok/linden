package todoapp.component

import linden.flowers.Component
import linden.flowers.Component.factoryBy
import linden.extension.bulma.BulmaHtml

import todoapp.statemanagement.{TodoState, TodoAction}

object TodoApp:
  def apply() = factoryBy[TodoApp]

class TodoApp(state: TodoState, action: TodoAction) extends Component:

  override def render = new Html with BulmaHtml:
    columns.withStyle(_.isCentered) {
      column.withStyle(_.is5) {
        div {
          +TodoItemCreatorUI()
        }
        panel("#TodoList") {
          generate(state.orderedTodos) { todo =>
            panelBlock("TodoBlock") {
              o style "display: block"
              +TodoItem(todo)
            }
          }
        }
        div {
          +ScoreUI()
        }
      }
    }
