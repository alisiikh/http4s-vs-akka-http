package com.alisiikh.akka.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.ActorMaterializer
import com.alisiikh.akka.directive.LoggingDirectives

class AppRoute(implicit mat: ActorMaterializer) {
  import Directives._
  import LoggingDirectives._

  def route: Route =
    post {
      logResponse {
        extractRequestEntity { entity =>
          onSuccess(entity.discardBytes().future()) { _ =>
            complete(StatusCodes.NoContent)
          }
        }
      }
    }
}
