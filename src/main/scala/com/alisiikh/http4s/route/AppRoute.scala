package com.alisiikh.http4s.route

import cats.effect.{ Effect, Timer }
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import scala.concurrent.duration.NANOSECONDS

class AppRoute[F[_]: Effect: Timer: Logger] {

  def route(implicit logger: Logger[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        for {
          startNanos        <- currentTimeNanos
          _                 <- req.body.compile.drain
          elapsedTimeMillis <- currentTimeNanos.map(endNanos => (endNanos - startNanos) / 1000000)
          resp              <- NoContent()
          _ <- logger.info(
            s"""Request: ${req.method} ${req.uri}, finished with ${resp.status}, took: $elapsedTimeMillis millis"""
          )
        } yield resp
    }
  }

  private def currentTimeNanos(implicit timer: Timer[F]): F[Long] = timer.clock.realTime(NANOSECONDS)
}
