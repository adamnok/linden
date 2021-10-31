package todoapp.component

import linden.flowers.dynamic.{awaitElementById, randomIdForDOM}
import org.scalajs.dom.raw.HTMLElement
import linden.flowers.Component
import linden.flowers.Component.factoryBy
import todoapp.*
import todoapp.statemanagement.*
import linden.store.Store
import linden.extension.bulma.BulmaHtml

object TodoItem:
  def apply(todo: Todo) = factoryBy[TodoItem](todo)

class TodoItem(todo: Todo)(action: TodoAction) extends Component:
  private val readyStore = Store(todo.ready)
  private val nameStore = Store(todo.name)
  private val changeNameStore = Store(false)
  
  override def mounted(): Unit =
    readyStore.subscribe(it => action.update(todo.dataCopy(ready = it)))

  private def enterOnNewName() =
    action.update(todo.dataCopy(name = nameStore.value))

  private def cancelOnNewName =
    nameStore.change(todo.name)
    changeNameStore.change(false)
  
  override def render = new Html with BulmaHtml:
    level {
      levelLeft {
        levelItem {
          input(readyStore)
        }
      }
      levelItem {
        generate(changeNameStore) {
          case false =>
            bulmaButton.withStyle(_.isWhite) {
              +todo.name
              o clazz "TodoItemName"
              e click changeNameStore.change(true)
              o style """
                position: relative;
                width: 100%;
                max-width: 100%;
                display: inline-block;
                text-align: left;
              """
            }
          case true =>
            +ChangeTodoItemName(todo)(cancelOnNewName)
        }
      }
      levelRight {
        levelItem {
          bulmaButton.withStyle(_.isDanger, _.isInverted) {
            o clazz "DeleteItem"
            i("fas fa-trash-alt"){}
            e click action.delete(todo)
          }
        }
        levelItem {
          div {
            div {
              bulmaButton.withStyle(_.isInfo, _.isInverted, _.isSmall) {
                i("fas fa-sort-up"){}
                e click action.goUp(todo)
              }
              
            }
            div {
              bulmaButton.withStyle(_.isInfo, _.isInverted, _.isSmall) {
                i("fas fa-sort-down"){}
                e click action.goBottom(todo)
              }
            }
          }
        }
      }
    }
