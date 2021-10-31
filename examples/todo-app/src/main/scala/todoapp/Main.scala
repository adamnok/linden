package todoapp

import scala.scalajs.js.annotation.JSExportTopLevel

import linden.di.DI
import linden.flowers.DOMRenderer

import todoapp.component.TodoApp
import todoapp.statemanagement.{TodoState, TodoAction}
import todoapp.util.{IdGenerator, I18N}
import todoapp.form.CreateTodoItemForm

@JSExportTopLevel("main")
def main() =
  val di = new DI:
    ref[IdGenerator] singleton IdGenerator()
    ref[I18N] singleton I18N()
    ref[TodoState] singleton TodoState()
    ref[TodoAction] singleton createInstance[TodoAction]
    ref[CreateTodoItemForm] singleton createInstance[CreateTodoItemForm]
  DOMRenderer(
    containerElementId = "app", // lehetne ez az alapértelmezett
    session = di,
    headComponent = TodoApp()
  )