package http

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as => Aas, _}
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import controller.ApplicationController
import db.Database
import domain.{TodoError, TodoItem, TodoName}
import zio.{IO, Runtime, ZIO}
import zio.internal.Platform

class Api(repo : Database) extends MarshallingSupport {

  //I'm so good at naming variables :)
   val idRoutes: Route = path("todo" / IntNumber) { id =>
    get {
      complete {
        val effect: ZIO[Database, TodoError, TodoItem] = ApplicationController.findTodoById(id)
        val result: IO[TodoError, TodoItem] = effect.provide(repo)
        result

        //this is what the scala compiler will help us do
        // complete ( Marshal(result).to[HttpResponse])(implicit marshaller : Marshaller[IO[TodoError,TodoItem])

      }
    } ~
      put {
        entity(Directives.as[TodoName]){todo =>
          val resultingEffect: IO[TodoError, StandardRoute] =
            ApplicationController.updateTodoById(id,todo)
              .provide(repo).map{ _ =>
            complete(HttpResponse(StatusCodes.OK))
          }
          resultingEffect

          // the compiler does this for us
          // standardRouteToRoute(resultingEffect)
        }
      } ~
     delete {
       ApplicationController.deleteTodoById(id).provide(repo).map{_ =>
         complete(HttpResponse(StatusCodes.OK))
       }
     }
  }


  val noId =  path("todo"){
    post {
      entity(Aas[TodoName]){todo =>
        ApplicationController.addTodo(todo).provide(repo).map{_ =>
         complete(HttpResponse(StatusCodes.Created))
       }
      }
    } ~ get {
      ApplicationController.getAllTodo.provide(repo).map{res =>
        complete(res)
      }
    }
  }


  lazy val routes = idRoutes ~ noId
  override val environment: Unit  =  Runtime.default.environment // environment type Unit,
  override val platform: Platform = zio.internal.Platform.default
}
