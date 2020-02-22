package com.alisiikh.http4s

import cats.effect._
import cats.implicits._
import com.alisiikh.http4s.route.AppRoute
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import fs2._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{ Logger => Http4sLogger }

object Http4sApp extends IOApp {

  implicit def unsafeLogger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def stream[F[_]: ConcurrentEffect: Timer: Logger]: Stream[F, _] =
    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve

  def httpApp[F[_]: ConcurrentEffect: Clock: Logger]: HttpApp[F] =
    Http4sLogger.httpApp(logHeaders = true, logBody = false) {
      new AppRoute[F].route.orNotFound
    }

  override def run(args: List[String]): IO[ExitCode] =
    stream[IO].compile.drain.as(ExitCode.Success)
}
