package example

import scala.scalajs.js.annotation.JSExportTopLevel
import linden.flowers.DOMRenderer

import example.component.HelloWorldComponent

@JSExportTopLevel("main")
def main() =
  DOMRenderer(
    containerElementId = "app",
    headComponent = HelloWorldComponent()
  )