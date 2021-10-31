package todoapp.component

import org.scalajs.dom.raw.HTMLElement
import linden.flowers.Component
import linden.flowers.Component.factory
import linden.flowers.Component.factoryBy
import todoapp.*
import todoapp.statemanagement.*
import linden.store.Store
import linden.extension.bulma.BulmaHtml
import linden.flowers.SelfResetting
import linden.flowers.dynamic.awaitElementById
import org.scalajs.dom.raw.HTMLElement
import lindovo.LindovoStoreForm
import todoapp.form.CreateTodoItem
import todoapp.form.CreateTodoItemW
import todoapp.form.CreateTodoItemForm
import lindovo.Validated
import lindovo.StoreCollectForForm
import todoapp.util.I18N

object TodoItemCreatorUI:
  def apply() = factory.resettable(factoryBy[TodoItemCreatorUI](_))

class TodoItemCreatorUI(
  reset: SelfResetting
)(
  todoForm: CreateTodoItemForm, action: TodoAction, i18n: I18N
) extends Component:

  val form = todoForm()

  inline def formNameError = form.$name
    .map(_.errors.map(i18n.apply).mkString(", "))

  private def onEnter =
    form.validatedOrError
      .map(_.name)
      .map(action.newTodo)
      .foreach(_ => reset())

  private def onCancel = reset()

  override def render = new Html with BulmaHtml:
    field {
      p("help is-danger") {
        o style "height: 20px;"
        +formNameError
      }
      control {
        inputCustom("#TodoName input is-large", form.name) {
          o style "background-color: #f6fbff;"
          o placeholder "add new todo item..."
          o clazz form.$name {
            case Validated(None, _) => ""
            case Validated(Some(v), Seq()) => "is-success"
            case Validated(Some(_), _) => "is-danger"
          }
          e enterKeyUp onEnter
          e escKeyUp onCancel
          awaitElementById("TodoName").foreach {
            case it: HTMLElement => it.focus()
          }
        }
      }
    }
