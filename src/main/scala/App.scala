import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import config.AppConfig
import db.postgres.PostgresDB
import http.Api
import zio.console._
import zio.{App => ZioApp, _}

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

object Main extends App {

  implicit val system = ActorSystem("TodoApp")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val routes = new Api(PostgresDB).routes
  val port  = Try(args(0).toInt).toOption.getOrElse(AppConfig.webPort)
  println(s"running on port $port")
  Http().bindAndHandle(routes,AppConfig.webHost,port)

}
/** A more functional way to start the App */
//object Main extends ZioApp {
//  override def run(args: List[String]): ZIO[Console, Nothing, Int] = {
//    (for {
//      as <- Managed.make(Task(ActorSystem("TodoApp")))(sys => Task.fromFuture(_ => sys.terminate()).ignore).use {
//        actorSystem =>
//          implicit val system: ActorSystem = actorSystem
//          implicit val materializer: ActorMaterializer = ActorMaterializer()
//          implicit val executionContext: ExecutionContextExecutor = system.dispatcher
//          val routes = new Api(PostgresDB).routes
//          val port  = Try(args.head.toInt).toOption.getOrElse(AppConfig.webPort)
//
//          putStrLn(s"Running on port $port").flatMap { _ =>
//            val tt = Http().bindAndHandle(routes,AppConfig.webHost,port)
//            ZIO.fromFuture(_ => tt).forever
//          }
//      }
//    } yield 0).fold(_ => 1, _ => 0)
//  }
//}
