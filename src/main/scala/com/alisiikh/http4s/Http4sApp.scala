package com.alisiikh.http4s

import cats.effect._
import cats.implicits._
import com.alisiikh.http4s.route.AppRoute
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{ Logger => Http4sLogger }

object Http4sApp extends IOApp {

  implicit def unsafeLogger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def resource[F[_]: ConcurrentEffect: Timer: Logger]: Resource[F, Server[F]] =
    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .resource

  def httpApp[F[_]: ConcurrentEffect: Clock: Logger]: HttpApp[F] =
    Http4sLogger.httpApp(logHeaders = true, logBody = true) {
      new AppRoute[F].route.orNotFound
    }

  override def run(args: List[String]): IO[ExitCode] =
    resource[IO]
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
