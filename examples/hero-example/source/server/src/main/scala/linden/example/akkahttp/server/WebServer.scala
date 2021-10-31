package linden.example.akkahttp.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import com.typesafe.config.ConfigFactory
import linden.example.akkahttp.server.services.{HeroService, WebService}

import scala.concurrent.ExecutionContext.Implicits._

object WebServer extends Directives {
  def main(args: Array[String]) = {

    implicit val system = ActorSystem("server-system")

    val config = ConfigFactory.load()
    val interface = config.getString("http.interface")
    val port = config.getInt("http.port")

    val service = new WebService()
    val heroService = new HeroService()

    Http().bindAndHandle(heroService.route ~ service.route, interface, port)

    println(s"Server online at http://$interface:$port")
  }
}





import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller.stringUnmarshaller
import akka.stream.Materializer
import upickle.default.{Reader, read}

import scala.concurrent.{ExecutionContext, Future}

class UPickleUnmarshaller[T: Reader] extends Unmarshaller[HttpRequest, T]:
  override def apply(request: HttpRequest)(using ExecutionContext, Materializer): Future[T] =
    stringUnmarshaller(request.entity).map(read[T](_))

trait UPickleDirective:
  given unMarshaller[T: Reader]: UPickleUnmarshaller[T] = new UPickleUnmarshaller[T]