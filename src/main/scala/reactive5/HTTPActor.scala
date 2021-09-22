package reactive5

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.model._
import akka.http.scaladsl.{Http, HttpExt}
import akka.util.ByteString

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object HTTPActor {
  sealed trait Message
  case class WrappedHttpResponse(response: HttpResponse) extends Message
  case object PoisonPill extends Message

  def start(http: HttpExt): Behavior[Message] = Behaviors.setup(context => {
    val http = Http(context.system)
    val request = http.singleRequest(HttpRequest(uri = "http://localhost:8080/hello"))

    context.pipeToSelf(request) {
      case Failure(exception) => PoisonPill
      case Success(value) => WrappedHttpResponse(value)
    }

    receive(http)
  })

  def receive(http: HttpExt): Behavior[Message] = Behaviors.receive((context, msg) => {
    implicit val system = context.system
    implicit val ec = context.executionContext

    msg match {
      case _ @ WrappedHttpResponse(HttpResponse(StatusCodes.OK, headers, entity, _)) =>
        val foldFuture = entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
          println("Got response, body: " + body.utf8String)
          PoisonPill
        }
        context.pipeToSelf(foldFuture)(_ => PoisonPill)
        Behaviors.same
      case _ @ WrappedHttpResponse(HttpResponse(code, _, _, _)) =>
        println("Request failed, response code: " + code)
        context.self ! PoisonPill
        Behaviors.same

      case PoisonPill =>
        Await.result(http.shutdownAllConnectionPools(),Duration.Inf)
        context.system.terminate()
        Behaviors.stopped
    }
  })
}

object Main extends App {
  val system = ActorSystem(Behaviors.empty, "http-system")
  val http = Http(system)
  val httpActor = system.systemActorOf(HTTPActor.start(http), "HTTP-Actor")

  Await.ready(system.whenTerminated, Duration.Inf)
}
