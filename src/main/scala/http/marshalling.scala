package http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.{Route, RouteResult, StandardRoute}
import domain._
import spray.json._
import zio.{IO, Runtime, ZIO}

import scala.concurrent.{Future, Promise}

trait MarshallingSupport
  extends SprayJsonSupport
    with DefaultJsonProtocol with Runtime[Unit] { self =>

  //implicit marshallers for my return Type A
  implicit val todoItemFormatter = jsonFormat2(TodoItem)
  implicit val todoNameFormatter = jsonFormat1(TodoName)

  sealed trait ErrorToHttpResponse[E] {
    def toHttpResponse(value: E): HttpResponse
  }

  private def generateHttpResponseFromError(error : TodoError) : HttpResponse = {
    error match {
      case ToDoItemError(errorMsg) =>
        HttpResponse(StatusCodes.NotFound, entity = HttpEntity(errorMsg))
      case QueryError(errorMsg) =>
        HttpResponse(StatusCodes.BadRequest, entity = HttpEntity(errorMsg))
      case DbError(errorMsg) =>
        HttpResponse(StatusCodes.InternalServerError, entity = HttpEntity(errorMsg))
    }
  }

  implicit def errorHttp = new ErrorToHttpResponse[TodoError] {
    override def toHttpResponse(value: TodoError): HttpResponse = {
      generateHttpResponseFromError(value)

    }
  }

  implicit val errorMarshaller: Marshaller[TodoError, HttpResponse] = {
    Marshaller { implicit ec => error   =>
         val response = generateHttpResponseFromError(error)
        PredefinedToResponseMarshallers.fromResponse(response)

    }
  }

  implicit def ioEffectToMarshallable[E, A](implicit m1: Marshaller[A, HttpResponse], m2: Marshaller[E, HttpResponse]): Marshaller[IO[E, A], HttpResponse] = {
    //Factory method for creating marshallers
    Marshaller { implicit ec =>
      effect =>

        val promise = Promise[List[Marshalling[HttpResponse]]]()
        val marshalledEffect: IO[Throwable, List[Marshalling[HttpResponse]]] = effect.foldM(
          err => IO.fromFuture(_ => m2(err)),
          suc => IO.fromFuture(_ => m1(suc))
        )
        self.unsafeRunAsync(marshalledEffect) { done =>
          done.fold(failed => promise.failure(failed.squash), success => promise.success(success))
        }
        promise.future
    }

  }

  implicit def standardRouteToRoute[E](effect: IO[E, StandardRoute])(implicit errToHttp: ErrorToHttpResponse[E]): Route = {
        //type Route = RequestContext ⇒ Future[RouteResult]
    ctx =>
      val promise = Promise[RouteResult]()

      val foldedEffect = effect.fold(
        err =>  { Future.successful(Complete(errToHttp.toHttpResponse(err))) },
        suc => suc.apply(ctx)
      )

      self.unsafeRunAsync(foldedEffect) { done =>
        done.fold(
          err => promise.failure(err.squash),
          suc => promise.completeWith(suc)

        )
      }

      promise.future
  }
}
