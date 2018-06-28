package com.emarsys.escher.akka.http

import akka.event.LoggingAdapter
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.emarsys.escher.EscherException
import spray.json._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Failure, Success}

trait EscherDirectives extends RequestBuilding with EscherAuthenticator {

  def signRequest(serviceName: String)(implicit ec: ExecutionContext, mat: Materializer): HttpRequest => Future[HttpRequest] = { r =>
    signRequestWithHeaders(Nil)(serviceName)(ec,mat)(r)
  }

  def signRequestWithHeaders(headers : List[HttpHeader])(serviceName:String)(implicit ec: ExecutionContext, mat: Materializer): HttpRequest => Future[HttpRequest] = { r =>

    val escher = setupEscher(createEscherForSigning(serviceName))
    val defaultSignedHeaders = escherConfig.headersToSign

    for {
      body <- Unmarshal(r.entity).to[String]
    } yield {
      val escherRequest: EscherHttpRequest = new EscherHttpRequest(r, body)
      escher.signRequest(
        escherRequest,
        escherConfig.key(serviceName),
        escherConfig.secret(serviceName),
        defaultSignedHeaders.union(headers.map(_.name)).asJava
      )
      escherRequest.getHttpRequest
    }
  }

  def escherAuthenticate[T](trustedServiceNames: List[String])(inner: String => Route)
                           (implicit ec: ExecutionContext, mat: Materializer, logger: LoggingAdapter): Route =  checkForwardedHttps {
    extract(_.request).map {
      case r: HttpRequest => authenticate(trustedServiceNames, r)
      case _              => Future.failed(new EscherException("Failed to parse HTTP request"))
    }.apply(onComplete(_) {
      case Success(value) => inner(value)
      case Failure(ex) =>
        logger.debug(ex.getMessage)
        reject(
          AuthenticationFailedRejection(
            AuthenticationFailedRejection.CredentialsRejected, HttpChallenge("Basic", "Escher")))
    })
  }

  def escherAuthenticateDirective(trustedServiceNames: List[String])
                                 (implicit ec: ExecutionContext, mat: Materializer, logger: LoggingAdapter): Directive0 =
    extract (_.request) map authenticate(trustedServiceNames) flatMap (onComplete(_)) flatMap passOrReject

  private def authenticate(trustedServiceNames: List[String])
                          (implicit ec: ExecutionContext, mat: Materializer): PartialFunction[HttpRequest, Future[String]] = {
    case r: HttpRequest if checkForwardedProtoHeader(r) => authenticate(trustedServiceNames, r)
    case _                                              => Future.failed(new EscherException("Failed to parse HTTP request"))
  }

  private def passOrReject(implicit logger: LoggingAdapter): PartialFunction[Try[String], Directive0] = {
    case Success(_)  => pass
    case Failure(ex) =>
      logger.debug(ex.getMessage)
      reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, HttpChallenge("Basic", "Escher")))
  }

  private val checkForwardedProtoHeader: HttpRequest => Boolean = _.headers find xForwardedProto forall mustBeHttps

  private val xForwardedProto: HttpHeader => Boolean = _.name.toLowerCase == "x-forwarded-proto"

  private val mustBeHttps: HttpHeader => Boolean = _.value().contains("https")

  def checkForwardedHttps(inner: Route) : Route = (ctx: RequestContext) => {
    val protocolHeader = ctx.request.headers.find(_.name.toLowerCase == "x-forwarded-proto")
    val notForwardedHttps = protocolHeader.fold(false)(! _.value().contains("https"))

    if (notForwardedHttps) {
      reject(ctx)
    } else {
      inner(ctx)
    }
  }

  def parseBody[T](body: String)(inner: T => Route)(implicit format: RootJsonFormat[T]): Route = {
    Try(body.parseJson.convertTo[T]) match {
      case Success(parsed) => inner(parsed)
      case Failure(x)      => reject(MalformedRequestContentRejection(x.getMessage, x.getCause))
    }
  }
}
