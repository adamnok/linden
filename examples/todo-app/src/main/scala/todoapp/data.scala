package todoapp


case class TodoData(
  name: String,
  ready: Boolean = false
)

case class Todo(
  id: Int,
  data: TodoData,
  order: Int = -1
):
  export data.{name, ready}

  def dataCopy(name: String = name, ready: Boolean = ready) =
    copy(data = TodoData(name = name, ready = ready))

  inline def eqData = (id, data)

case class Score(ready: Int, unReady: Int)