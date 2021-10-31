package example.form.client.component

import example.form.client.form.HeroForm
import example.form.client.resolver.UrlResolver
import linden.flowers.Component
import lindovo.Validated
import org.scalajs.dom.console
import org.scalajs.dom.ext.Ajax
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits._

object HeroExampleInput:
  def apply() = Component.factoryBy[HeroExampleInput]

final class HeroExampleInput(
  urlResolver: UrlResolver,
  heroForm: HeroForm
) extends Component {

  private val hero = heroForm()

  private def clickOnCreate(): Unit = {
    hero.get.foreach { hero =>
      console.log(hero.toString)
      Ajax
        .post(
          url = urlResolver.heroRegister,
          data = write(hero)
        )
        .foreach { it =>
          console.log(it.toString)
        }
    }
  }

  override lazy val render: Html = new Html {
    div {
      div {
        input(hero.store.name)
        generate(hero.formValidatedStore.name) {
          _.errors.foreach { it =>
            div {
              +it.message
            }
          }
        }
      }
      div {
        input(hero.store.power)
        generate(hero.formValidatedStore.power) {
          _.errors.foreach { it =>
            div {
              +it.message
            }
          }
        }
      }
      div { 
        input(hero.store.age)
        generate(hero.formValidatedStore.age) {
          _.errors.foreach { it =>
            div {
              +it.message
            }
          }
        }
      }
      div {
        generate(hero.validatedStore) {
          value =>
            if (value.isDefined)
              a {
                e.click{ clickOnCreate()}
                +"OK - click to submit"
                div {
                  +value.toString
                }
              }
            else
              div {
                +"Nem OK"
              }
        }
      }
    }
  }
}
