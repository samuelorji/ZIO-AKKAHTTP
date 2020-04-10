import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import config.AppConfig
import db.postgres.PostgresDB
import http.Api

object App extends App { // extending scala's App trait

  implicit val system = ActorSystem("TodoApp")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val routes = new Api(PostgresDB).routes

  Http().bindAndHandle(routes,AppConfig.webHost,AppConfig.webPort)

}
