package example.component

import linden.flowers.{BrowserLocator, Component, Rout, Router}
import linden.flowers.Component.factoryBy

object AppComponent:
  def apply() = factoryBy[AppComponent]

class AppComponent(locator: BrowserLocator) extends Component:

  private def goToA: Unit = locator to "/a"

  private def goToB: Unit = locator to "/b"

  private def goToC: Unit = locator to "/c"

  override def render = new Html:
    div {
      ul {
        li {
          a {
            +"go to A"
            e click goToA
          }
        }
        li {
          a {
            +"go to B"
            e click goToB
          }
        }
        li {
          a {
            +"go to C"
            e click goToC
          }
        }
      }
    }
    section {
      +Router(
        Rout("/a") { _ =>
          "A" -> AComponent()
        },
        Rout("/b") { _ =>
          "B" -> BComponent()
        },
        Rout("/c") { _ =>
          "A" -> CComponent()
        }
      )
    }
