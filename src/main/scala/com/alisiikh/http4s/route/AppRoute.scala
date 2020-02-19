package com.alisiikh.http4s.route

import cats.effect.{ Clock, Effect }
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import scala.concurrent.duration.NANOSECONDS

class AppRoute[F[_]](implicit F: Effect[F]) {

  def route(implicit clock: Clock[F], logger: Logger[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        for {
          startNanos        <- clock.realTime(NANOSECONDS)
          _                 <- req.body.compile.drain
          elapsedTimeMillis <- clock.realTime(NANOSECONDS).map(endNanos => (endNanos - startNanos) / 1000000)
          resp              <- NoContent()
          _ <- logger.info(
            s"""Request: ${req.method} ${req.uri}, finished with ${resp.status}, took: $elapsedTimeMillis millis"""
          )
        } yield resp
    }
  }
}
