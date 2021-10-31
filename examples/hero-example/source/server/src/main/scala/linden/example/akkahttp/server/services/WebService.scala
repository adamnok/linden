package linden.example.akkahttp.server.services

import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
//import linden.example.akkahttp.server.html.index
//import linden.example.akkahttp.server.twirl.Implicits._


class WebService(implicit val materializer: Materializer) extends Directives {

  val route: Route = {
    concat(
      pathSingleSlash {
        get (getFromResource("index.html"))
      },
      pathPrefix("assets" / Remaining) { file =>
        encodeResponse {
          getFromResource("assets/" + file)
        }
      }
    )
  }
}
