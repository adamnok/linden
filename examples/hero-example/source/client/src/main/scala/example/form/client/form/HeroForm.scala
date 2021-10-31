package example.form.client.form

import example.form.client.resolver.UrlResolver
import example.form.shared.Hero
import example.form.shared.HeroWorkTemplate
import linden.store.ClearContext
import lindovo.LindovoStoreForm
import org.scalajs.dom.ext.Ajax
import upickle.default._
import lindovo.FieldValidator
import scala.concurrent.ExecutionContext.Implicits._
import lindovo.Validator

class HeroForm(urlResolver: UrlResolver):

  def apply()(using ClearContext) =
    LindovoStoreForm[Hero](storeBuilder)

  def storeBuilder(m: HeroWorkTemplate[FieldValidator]) =
    import m.*
    copy(name = name.register(nameIsNotRegistered))

  private def nameIsNotRegistered(name: String) =
    Ajax
      .get(url = urlResolver.heroCheckExistName(name))
      .map { response =>
        Option.when(read[Boolean](response.responseText)) {
          "Ilyen néven már regisztráltak hőst!"
        }
      }
