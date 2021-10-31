package example

import scala.scalajs.js.annotation.JSExportTopLevel

import linden.flowers.DOMRenderer

import example.component.AppComponent

@JSExportTopLevel("main")
def main(): Unit =
  DOMRenderer(
    containerElementId = "app",
    headComponent = AppComponent()
  )
