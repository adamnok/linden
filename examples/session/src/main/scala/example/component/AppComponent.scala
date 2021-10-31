package example.component

import linden.store.Store
import linden.flowers.{Component}
import linden.flowers.Component.factoryBy
import linden.di.{DI, DIGraph}

import example.Data

object AppComponent:
  def apply() = factoryBy[AppComponent]

class AppComponent() extends Component:

  private val store = Store.option[Data]

  private def createSession(data: Data)(parent: DI) =
    new DIGraph(parent):
      ref[Data] singleton(data)

  override def render = new Html:
    nav {
      a {
        +"A data"
        e click store.change(Data("A data"))
      }
      a {
        +"B data"
        e click store.change(Data("B data"))
      }
    }
    section {
      generate(store){
        case Some(data) =>
          session(createSession(data)) {
            +AComponent()
          }
        case None =>
      }
    }
