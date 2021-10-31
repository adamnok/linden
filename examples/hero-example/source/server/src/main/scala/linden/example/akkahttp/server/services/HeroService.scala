package linden.example.akkahttp.server.services

import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import example.form.shared.Hero
import example.form.shared.Hero.given
import example.form.shared.HeroWorkTemplate
import upickle.default._
import lindovo.LindovoForm
import scala.util.{Failure, Success}
import lindovo.FormMapping
import scala.concurrent.ExecutionContext.Implicits._
import linden.example.akkahttp.server.UPickleUnmarshaller
import linden.example.akkahttp.server.UPickleDirective


object HeroService:
  private var heros: List[Hero] = List.empty[Hero]

class HeroService(using Materializer) extends Directives, UPickleDirective:
  val route: Route =
    pathPrefix("hero") {
      concat(
        (path("exists") & get) {
            parameter("name") { name =>
              println(s"Exists: $name, ${HeroService.heros}")
              val result = HeroService.heros.exists(_.name == name)
              complete(write(result))
            }
        },
        (path("register") & post) {
          entity(as[Hero]) { hero =>
            onComplete(LindovoForm(hero).getValidated) {
              case Success(Left(acceptedHero)) =>
                HeroService.heros :+= acceptedHero
                complete("OK")
              case Success(Right(errors)) =>
                println("ERROR: " + errors.toString)
                complete("OK")
              case Failure(exception) =>
                println(exception)
                complete("OK")
            }
          }
        }
      )
    }