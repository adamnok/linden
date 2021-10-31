package todoapp.statemanagement

import todoapp.Todo


extension (todos: Seq[Todo])

  def findById(v: Todo) = todos.find(_.id == v.id)

  def filterNotById(v: Todo) = todos.filter(_.id != v.id)

  def addOrUpdate(v: Todo): Seq[Todo] =
    val value = findById(v) match
      case Some(value) => v.copy(order = value.order + v.order)
      case None => v
    filterNotById(value) :+ value
    
  def toNameSet = todos.map(_.name).toSet