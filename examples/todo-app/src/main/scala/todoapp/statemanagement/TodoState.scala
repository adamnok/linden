package todoapp.statemanagement

import linden.store.Store
import linden.store.mutable.SimpleStore
import linden.store.functional.State
import linden.flowers.optimizing.{optimize, RenderStrategy}

import todoapp.util.IdGenerator
import todoapp.{Todo, Score}

final class TodoState extends State:
  private[statemanagement] val todos = Store(Seq.empty[Todo])
    .wrapper(
      forRead = identity[Seq[Todo]],
      forWrite = _.reIndexSort
    )
    .asFunctional

  def orderedTodos = todos
    .map(_.sortBy(_.order))
    .map(_.map(_.copy(order = 0)))
    .optimize(RenderStrategy.indexable(), _.eqData)

  def names = todos.map(_.toNameSet)

  def names(ignore: Todo) = todos
    .map(_.filterNotById(ignore))
    .map(_.toNameSet)

  def score = todos
    .map { it =>
      Score(
        ready = it.count(_.ready),
        unReady = it.count(! _.ready)
      )
    }

  extension (todos: Seq[Todo])
    private def reIndexSort = todos
      .sortBy(_.order)
      .zipWithIndex
      .map((it, index) => it.copy(order = index * 2))
end TodoState