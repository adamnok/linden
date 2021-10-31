package todoapp.component

import org.scalajs.dom.raw.HTMLElement
import linden.flowers.Component
import linden.flowers.Component.factoryBy
import todoapp.*
import todoapp.statemanagement.TodoAction
import linden.extension.bulma.BulmaHtml
import linden.flowers.dynamic.{awaitElementById, randomIdForDOM}
import todoapp.form.CreateTodoItemForm
import lindovo.Validated

object ChangeTodoItemName:
  def apply(todo: Todo)(cancel:  => Unit) = factoryBy[ChangeTodoItemName](todo, cancel)

class ChangeTodoItemName(todo: Todo, cancel:  => Unit)(
  todoForm: CreateTodoItemForm, action: TodoAction
) extends Component:

  val form = todoForm(todo)

  val id = randomIdForDOM("todo-item_")

  override def mounted(): Unit = form.name.change(todo.name)

  private def onEnter =
    form.validatedOrError
      .map(_.name)
      .map(it => todo.dataCopy(name = it))
      .foreach(action.update)

  private def onCancel = cancel

  override def render = new Html with BulmaHtml:
    inputCustom(s"#$id input", form.name) {
      o placeholder "change name or press esc"
      o clazz form.$name {
        case Validated(None, _) => ""
        case Validated(Some(v), Seq()) => "is-success"
        case Validated(Some(_), _) => "is-danger"
      }
      e enterKeyUp onEnter
      e escKeyUp onCancel
      awaitElementById(id).foreach {
        case it: HTMLElement => it.focus()
      }
    }
