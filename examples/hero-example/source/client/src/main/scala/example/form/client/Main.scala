package example.form.client

import example.form.client.component.MainComponent
import example.form.client.form.HeroForm
import example.form.client.resolver.UrlResolver
import linden.di.DI
import linden.flowers.DOMRenderer
import org.scalajs.dom.console

import scala.scalajs.js.annotation.JSExportTopLevel


private def di = new DI:
  ref[UrlResolver] singleton new UrlResolver
  ref[HeroForm] singleton createInstance[HeroForm]

@JSExportTopLevel("main")
def main(): Unit =
  console.info("Hello from ScalaJS")

  DOMRenderer(
    containerElementId = "app",
    session = di,
    headComponent = MainComponent()
  )
