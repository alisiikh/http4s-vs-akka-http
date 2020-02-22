package com.alisiikh.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.alisiikh.akka.route.AppRoute
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object AkkaHttpApp extends App with StrictLogging {

  val config =
    ConfigFactory
      .parseString(
        """akka {
          |  loggers = ["akka.event.slf4j.Slf4jLogger"]
          |  loglevel = DEBUG
          |  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
          |
          |  http {
          |    server {
          |      parsing.max-content-length = 10g
          |    }
          |    client {
          |      parsing.max-content-length = 10g
          |    }
          |  }
          |}
          |""".stripMargin
      )
      .withFallback(ConfigFactory.load())

  implicit val system: ActorSystem  = ActorSystem("akka-http", config)
  implicit val mat: Materializer    = Materializer(system)
  implicit val ec: ExecutionContext = system.dispatcher

  val host     = "0.0.0.0"
  val port     = 8080
  val appRoute = new AppRoute

  Http()
    .bindAndHandle(appRoute.route, host, port)
    .map { binding =>
      logger.info(s"Started server at: $host:$port")
      binding
    }
    .recover {
      case NonFatal(ex) =>
        logger.error(s"Failed to start server at: $host:$port", ex)
        sys.exit(1)
    }
}
