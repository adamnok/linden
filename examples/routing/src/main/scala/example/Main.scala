package example

import scala.scalajs.js.annotation.JSExportTopLevel

import linden.di.DI
import linden.flowers.DOMRenderer
import linden.flowers.di.RoutingPlugin

import example.component.AppComponent

@JSExportTopLevel("main")
def main(): Unit =

  val di = new DI:
    use(RoutingPlugin)

  DOMRenderer(
    session = di,
    containerElementId = "app",
    headComponent = AppComponent()
  )
