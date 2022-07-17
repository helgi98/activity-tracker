package org.helgi.activity.auth

import io.circe.generic.auto._
import io.grpc.Metadata
import org.helgi.activity.config.AppConfig
import org.helgi.activity.model.User
import scalapb.zio_grpc.RequestContext
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.model.Uri
import zio.{IO, Task, ZIO, ZLayer}

final case class AuthError(msg: String)

case class AuthProvider(config: AppConfig, backend: SttpBackend[Task, Any]) {

  private val tokenKey: Metadata.Key[String] = Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER)

  def authenticate(rc: RequestContext): IO[AuthError, User] = {
    for {
      tokenOpt <- rc.metadata.get(tokenKey)
      token <- ZIO.fromOption(tokenOpt).mapError(_ => AuthError("Token not found"))
      request = basicRequest.get(Uri(config.auth.url))
        .auth.bearer(token)
        .response(asJson[User])
      response <- backend.send(request).refineOrDie {
        case t: Throwable => AuthError(t.getMessage)
      }
      user <- if (!response.isSuccess) ZIO.fail(AuthError(s"Authentication failed with: ${response.statusText}"))
      else ZIO.fromEither(response.body).mapError(_ => AuthError("Failed to parse auth response"))
    } yield user
  }

}

object AuthProvider {
  val live = ZLayer.fromFunction(AuthProvider.apply _)
}
