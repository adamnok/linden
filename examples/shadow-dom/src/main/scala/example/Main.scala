package example

import scala.scalajs.js.annotation.JSExportTopLevel
import linden.flowers.{Component, DOMRenderer}

class HelloWorldComponent() extends Component:
  override def render = new Html:
    div {
      shadow {
        div {
          +"Hello, world!"
        }
      }
    }

@JSExportTopLevel("main")
def main() =
  DOMRenderer(
    containerElementId = "app",
    headComponent = HelloWorldComponent()
  )
