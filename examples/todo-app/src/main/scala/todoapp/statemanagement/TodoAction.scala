package todoapp.statemanagement

import scala.language.implicitConversions

import linden.store.functional.Action
import linden.store.mutable.SimpleStore

import todoapp.{Todo, TodoData}
import todoapp.util.IdGenerator

final class TodoAction(state: TodoState, id: IdGenerator) extends Action:
 
  extension (store: SimpleStore[Seq[Todo], Seq[Todo]])
    private infix inline def += (value: Todo) = store.change(_.addOrUpdate(value))
    private infix inline def -= (value: Todo) = store.change(_.filterNotById(value))

  def newTodo(name: String) = state.todos += Todo(id(), TodoData(name))

  def moveTo(move: Int)(todo: Todo) = state.todos += todo.copy(order = todo.order + move)

  inline def goUp = moveTo(-3)

  inline def goBottom = moveTo(3)

  def update(todo: Todo) = state.todos += todo

  def delete(todo: Todo) = state.todos -= todo

end TodoAction