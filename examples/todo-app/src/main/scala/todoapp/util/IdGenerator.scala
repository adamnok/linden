package todoapp.util

class IdGenerator:
  private var nextId = 0
  def apply() = this.synchronized {
    val current = nextId
    nextId = nextId + 1
    current
  }