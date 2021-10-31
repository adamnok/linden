package example.functionalstore.client

import example.functionalstore.client.component.MainComponent
import example.functionalstore.client.state.{HeroState, HeroStateActions}
import linden.di.DI
import linden.di.DIMacros.ref
import linden.flowers.DOMRenderer

import scala.scalajs.js.annotation.JSExportTopLevel

private lazy val di = new DI:
  ref[HeroState] singleton new HeroState()
  ref[HeroStateActions] singleton new HeroStateActions(apply(ref[HeroState]))

@JSExportTopLevel("main")
def main(): Unit = 
  DOMRenderer(
    containerElementId = "app",
    session = di,
    headComponent = new MainComponent()
  )
