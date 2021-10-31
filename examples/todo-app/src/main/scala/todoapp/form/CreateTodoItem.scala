package todoapp.form

import scala.concurrent.ExecutionContext 

import linden.store.{ReadStore, ClearContext}
import lindovo.FieldValidator
import lindovo.FieldValidators.*
import lindovo.FormMapping
import lindovo.LindovoStoreForm
import lindovo.LindovoStoreForm.Options
import lindovo.FieldValidator.NonCacheable

import todoapp.Todo
import todoapp.statemanagement.TodoState

case class CreateTodoItem(name: String)
case class CreateTodoItemW[T[_]](name: T[String])

object CreateTodoItem:
  private def builder(m: CreateTodoItemW[FieldValidator]) =
    m.copy(
      name = m.name
        .register(isRequire)
        .register(minTextLength(3), maxTextLength(30))
    )
  given FormMapping[CreateTodoItem, CreateTodoItemW](builder)


final class CreateTodoItemForm(state: TodoState):
  def apply()(using ClearContext, ExecutionContext) =
    val s = state.names
    given Options = Options(externalStores = Seq(s))
    LindovoStoreForm[CreateTodoItem](builder(s.value))

  def apply(current: Todo)(using ClearContext, ExecutionContext) =
    val s = state.names(current)
    given Options = Options(externalStores = Seq(s))
    LindovoStoreForm[CreateTodoItem](builder(s.value))

  private def builder(registered:  => Set[String])(m: CreateTodoItemW[FieldValidator]) =
    m.copy(
      name = m.name
        .register(NonCacheable(nonRegistered(registered)))
    )

  private def nonRegistered(registered: => Set[String])(it: String) =
    Option.when(registered contains it)("create.todo.item.nameIsReserved")
    