package com.alisiikh

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Sync, Timer}
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import cats.syntax.apply._
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.log4s.getLogger

object Http4sApp extends IOApp {

  def stream[F[_] : ConcurrentEffect : Sync : Timer : ContextShift]: Stream[F, Nothing] = {
    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
    }.drain

  def httpApp[F[_] : ConcurrentEffect]: HttpApp[F] = {
    Logger.httpApp(logHeaders = true, logBody = true) {
      Route.route[F].orNotFound
    }
  }

  override def run(args: List[String]): IO[ExitCode] =
    stream[IO].compile.drain *> IO.pure(ExitCode.Success)
}

object Route {

  private[this] val logger = getLogger

  def route[F[_]](implicit F: ConcurrentEffect[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req@(POST -> Root) =>
        // TODO: safely log the result
        req.body.compile.drain *> Sync[F].pure(NoContent)
    }
  }
}