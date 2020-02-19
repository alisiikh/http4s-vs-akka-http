package com.alisiikh.akka.directive

import akka.event.Logging.LogLevel
import akka.event.{ Logging, LoggingAdapter }
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.RouteResult.{ Complete, Rejected }
import akka.http.scaladsl.server.directives.{ DebuggingDirectives, LogEntry, LoggingMagnet }
import akka.http.scaladsl.server.{ Directive0, RouteResult }

trait LoggingDirectives extends DebuggingDirectives {

  def logResponse: Directive0 =
    DebuggingDirectives.logRequestResult(LoggingMagnet(logResponse(captureTime)))

  private def captureTime: Long = System.nanoTime()

  private def logResponse(
      reqTime: Long,
      logLevel: LogLevel = Logging.InfoLevel
  )(logAdapter: LoggingAdapter): HttpRequest => RouteResult => Unit = { req => result =>
    val entry = result match {
      case Complete(resp) =>
        val elapsedTimeMillis = (captureTime - reqTime) / 1000000
        val loggingString =
          s"""Request: ${req.method.value} ${req.uri}, finished with ${resp.status.intValue}, took: $elapsedTimeMillis millis"""
        LogEntry(loggingString, logLevel)
      case Rejected(reason) =>
        LogEntry(s"Request rejected, reason: ${reason.mkString(",")}", logLevel)
    }
    entry.logTo(logAdapter)
  }
}
object LoggingDirectives extends LoggingDirectives
