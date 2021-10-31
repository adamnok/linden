package example

import scala.scalajs.js.annotation.JSExportTopLevel
import linden.flowers.DOMRenderer

import example.component.GiveMeYourNameComponent


@JSExportTopLevel("main")
def main() =
  DOMRenderer(
    containerElementId = "app",
    headComponent = new GiveMeYourNameComponent()
  )
