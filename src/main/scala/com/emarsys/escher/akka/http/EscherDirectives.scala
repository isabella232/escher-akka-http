package com.emarsys.escher.akka.http

import akka.event.LoggingAdapter
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.emarsys.escher.EscherException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait EscherDirectives extends RequestBuilding with EscherAuthenticator {

  def signRequest(serviceName: String)(implicit ec: ExecutionContext, mat: Materializer): (HttpRequest) => Future[HttpRequest] = { r =>

    val escher = createEscherRequest()

    for {
      body <- Unmarshal(r.entity).to[String]
    } yield {
      val escherRequest: EscherHttpRequest = new EscherHttpRequest(r, body)
      escher.signRequest(
        escherRequest,
        escherConfig.key(serviceName),
        escherConfig.secret(serviceName),
        java.util.Arrays.asList("host", "X-Ems-Date")
      )
      escherRequest.getHttpRequest
    }
  }

  def escherRoute[T](serviceName: List[String])(inner: (String) => Route)(implicit ec: ExecutionContext, mat: Materializer, logger: LoggingAdapter): Route = {
    extract(_.request).map {
      case r: HttpRequest => authenticate(serviceName, r)
      case msg            => Future.failed(new EscherException("Failed to parse HTTP request"))
    }.apply(onComplete(_) {
      case Success(value) => inner(value)
      case Failure(ex) =>
        logger.debug(ex.getMessage)
        reject(
          AuthenticationFailedRejection(
            AuthenticationFailedRejection.CredentialsRejected, HttpChallenge("Basic", "Escher")))
    })
  }
}
