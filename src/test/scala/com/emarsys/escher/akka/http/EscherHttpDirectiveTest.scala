package com.emarsys.escher.akka.http

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.emarsys.escher.akka.http.config.EscherConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import spray.json.DefaultJsonProtocol


class EscherHttpDirectiveTest
  extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with DefaultJsonProtocol
    with ScalaFutures
    with EscherDirectives {


  private val forwardedHttpsRequest = HttpRequest(
    method = HttpMethods.GET,
    uri = Uri("/"),
    headers = List(RawHeader("host", "trunk.suite.ett.local"), RawHeader("X-Forwarded-Proto", "https"))
  )

  private val forwardedHttpRequest = HttpRequest(
    method = HttpMethods.GET,
    uri = Uri("/"),
    headers = List(RawHeader("host", "trunk.suite.ett.local"), RawHeader("X-Forwarded-Proto", "http"))
  )

  implicit val logger = system.log

  def route: Route =
    (get & pathSingleSlash)(checkForwardedHttps(complete("OK"))) ~
    (get & path("path"))(escherAuthenticate(escherConfig.services)(_ => complete("OK")))

  def routeWithDirective: Route =
    (get & path("path") & escherAuthenticateDirective(escherConfig.services))(complete("OK"))

  override val escherConfig: EscherConfig =
    new EscherConfig(com.typesafe.config.ConfigFactory.load().getConfig("escher"))

  "checkForwardedHttps" should {
     "reject forwarded http request" in {
        forwardedHttpRequest ~> route ~> check {
          handled shouldBe false
        }
    }

    "accept forwarded https request" in {
      forwardedHttpsRequest ~> route ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }

    "accept normal http request" in {
      Get("/") ~> route ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }
  }

  "escherAuthenticate" should {
    val service = escherConfig.services.head
    val (key, secret) = (escherConfig.key(service), escherConfig.secret(service))
    val host = s"${escherConfig.hostName}:${escherConfig.port}"

    "accept signed requests" in {
      val signed = signRequest(service)(executor, materializer)(_:HttpRequest).futureValue
      signed(Get(s"https://$host/path?query=param").withHeaders(List(RawHeader("host", host)))) ~> route ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }

    "accept requests with presigned urls" in {
      val escher = setupEscher(createEscherForSigning(service))
      val presignedUrl = escher.presignUrl(s"https://$host/path?query=param", key, secret)

      Get(presignedUrl).withHeaders(List(RawHeader("host", host))) ~> route ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }
  }

  "escherAuthenticateDirective" should {
    val service = escherConfig.services.head
    val (key, secret) = (escherConfig.key(service), escherConfig.secret(service))
    val host = s"${escherConfig.hostName}:${escherConfig.port}"

    val sign = signRequest(service)(executor, materializer)(_:HttpRequest).futureValue

    "reject forwarded http request" in {
      val request = Get(s"http://$host/path?query=param")
        .withHeaders(
          List(
            RawHeader("X-Forwarded-Proto", "http"),
            RawHeader("host", host)
          )
        )

      sign(request) ~> routeWithDirective ~> check {
        handled shouldBe false
      }
    }

    "accept signed requests" in {
      val request = Get(s"https://$host/path?query=param").withHeaders(List(RawHeader("host", host)))

      sign(request) ~> routeWithDirective ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }

    "signed and forwarded https request" in {
      val request = Get(s"https://$host/path?query=param")
        .withHeaders(
          List(
            RawHeader("X-Forwarded-Proto", "https"),
            RawHeader("host", host)
          )
        )

      sign(request) ~> routeWithDirective ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }

    "accept requests with pre-signed urls" in {
      val escher = setupEscher(createEscherForSigning(service))
      val preSignedUrl = escher.presignUrl(s"https://$host/path?query=param", key, secret)
      val request = Get(preSignedUrl).withHeaders(List(RawHeader("host", host)))

      request ~> routeWithDirective ~> check {
        handled shouldBe true
        status shouldBe OK
      }
    }
  }
}
