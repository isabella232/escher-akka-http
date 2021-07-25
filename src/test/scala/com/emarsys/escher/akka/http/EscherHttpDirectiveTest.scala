package com.emarsys.escher.akka.http

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.emarsys.escher.akka.http.config.EscherConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext

class EscherHttpDirectiveTest
  extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with ScalaFutures
    with EscherDirectives {

  implicit val logger: LoggingAdapter = system.log

  def route: Route =
    (get & path("path") & escherAuthenticate(escherConfig.services))(complete("OK")) ~
    (get & path("path2") & escherAuthenticate(escherConfig.services, true))(complete("OK"))

  def justForValidation = escherAuthenticate(escherConfig.services){complete("OK")}

  override val escherConfig: EscherConfig =
    new EscherConfig(com.typesafe.config.ConfigFactory.load().getConfig("escher"))

  "#escherAuthenticate" should {
    val service = escherConfig.services.head
    val (key, secret) = (escherConfig.key(service), escherConfig.secret(service))
    val host = s"${escherConfig.hostName}:${escherConfig.port}"

    val sign = signRequest(service)(executor, materializer)(_:HttpRequest).futureValue

    "reject forwarded http request" in {
      val request = Get(s"http://$host/path?query=param")
        .withHeaders(List(
          RawHeader("X-Forwarded-Proto", "http"),
          RawHeader("host", host)
        ))

      sign(request) ~> route ~> check {
        handled shouldBe false
      }
    }

    "accept signed requests" in {
      val request = Get(s"https://$host/path?query=param").withHeaders(List(RawHeader("host", host)))

      sign(request) ~> route ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }

    "accept requests signed with passive keys" in {
      val request = Get(s"https://$host/path?query=param").withHeaders(List(RawHeader("host", host)))
      val serviceWithKeyPool = "service1"
      val keyPool = escherConfig.keyPool(serviceWithKeyPool)
      val activeKey: String = escherConfig.key(serviceWithKeyPool)

      val (passiveKey, passiveSecret) = keyPool.find{ case (key, _) => key != activeKey }.get

      signWithCredentials(serviceWithKeyPool, passiveKey, passiveSecret)(request) ~> route ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }

    "sign and forward https request" in {
      val request = Get(s"https://$host/path?query=param")
        .withHeaders(List(
          RawHeader("X-Forwarded-Proto", "https"),
          RawHeader("host", host)
        ))

      sign(request) ~> route ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }

    "sign and forward http request if the forwarded proto http allowed" in {
      val request = Get(s"https://$host/path2?query=param")
        .withHeaders(List(
          RawHeader("X-Forwarded-Proto", "http"),
          RawHeader("host", host)
        ))

      sign(request) ~> route ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }

    "accept requests with pre-signed urls" in {
      val escher = setupEscher(createEscherForSigning(service))
      val preSignedUrl = escher.presignUrl(s"https://$host/path?query=param", key, secret)
      val request = Get(preSignedUrl).withHeaders(List(RawHeader("host", host)))

      request ~> route ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }
  }

  private def signWithCredentials(serviceName:String, key: String, secret: String)(request: HttpRequest)(
    implicit ec: ExecutionContext
  ): HttpRequest = {

    val escher = setupEscher(createEscherForSigning(serviceName))
    val defaultSignedHeaders = escherConfig.headersToSign

    for {
      body <- Unmarshal(request.entity).to[String]
    } yield {
      val escherRequest: EscherHttpRequest = new EscherHttpRequest(request, body)
      escher.signRequest(
        escherRequest,
        key,
        secret,
        defaultSignedHeaders.asJava
      )
      escherRequest.getHttpRequest
    }
  }.futureValue
}
