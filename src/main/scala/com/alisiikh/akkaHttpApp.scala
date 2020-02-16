package com.alisiikh

import akka.actor.ActorSystem
import akka.event.Logging.LogLevel
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.{Directive0, Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

object AkkaUnsafeApp extends App with StrictLogging {
  implicit val system: ActorSystem = ActorSystem("akka-http")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = ExecutionContext.global

  {
    for {
      route <- Future(Route.route)
      binding <- Http().bindAndHandle(route, "0.0.0.0", "8080")

      _ = logger.info("Started server at: 0.0.0.0:8080")
    } yield binding
  }.recover {
    case NonFatal(ex) =>
      logger.error("Failed to start server at: 0.0.0.0:8080", ex)
      sys.exit(1)
  }
}

object Route extends LoggingDirectives {

  def route(implicit ec: ExecutionContext, mat: ActorMaterializer): Route =
    post {
      extractRequest { req =>
        completeLoggingResponse {
          req.entity
            .dataBytes
            .runWith(Sink.ignore)
            .map(_ => StatusCodes.NoContent)
        }
      }
    }
}

trait LoggingDirectives extends DebuggingDirectives {

  def completeLoggingResponse(m: ToResponseMarshallable): Route = logResponse & complete(m)
  def logResponse: Directive0 = DebuggingDirectives.logRequestResult(LoggingMagnet(logResponseHelper(currentTime)))

  private def currentTime: Long = System.nanoTime()

  private def logResponseHelper(reqTime: Long, logLevel: LogLevel = Logging.InfoLevel)
                               (logAdapter: LoggingAdapter)(req: HttpRequest)(resp: RouteResult): Unit = {
    val entry = resp match {
      case Complete(resp) =>
        val respTime = currentTime
        val elapsedTimeMillis = (respTime - reqTime) / 1000000
        val loggingString = s"""Request: ${req.method} ${req.uri} ${resp.status} -> $elapsedTimeMillis"""
        LogEntry(loggingString, logLevel)
      case Rejected(reason) =>
        LogEntry(s"Request rejected, reason: ${reason.mkString(",")}", logLevel)
    }
    entry.logTo(logAdapter)
  }
}
