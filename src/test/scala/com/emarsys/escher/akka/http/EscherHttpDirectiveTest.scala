package com.emarsys.escher.akka.http

import akka.http.scaladsl.model.{HttpMethods, Uri}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.emarsys.escher.akka.http.config.EscherConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{path => _, _}
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
    headers = List(RawHeader("host", "trunk.suite.ett.local"), RawHeader("x-forwarded-proto", "https"))
  )

  private val forwardedHttpRequest = HttpRequest(
    method = HttpMethods.GET,
    uri = Uri("/"),
    headers = List(RawHeader("host", "trunk.suite.ett.local"), RawHeader("x-forwarded-proto", "http"))
  )

  def route: Route = (get & pathSingleSlash)(checkForwardedHttps(complete("OK")))
  override val escherConfig: EscherConfig = null


  "checkForwardedHttps" should {
     "reject forwarded http request" in {
        forwardedHttpRequest ~> route ~> check {
          handled shouldBe false
        }
    }

    "accept forwarded https request" in {
      forwardedHttpsRequest ~> route ~> check {
        handled shouldBe true
        responseAs[String] shouldEqual "OK"
      }
    }

    "accept normal http request" in {
      Get("/") ~> route ~> check {
        handled shouldBe true
        responseAs[String] shouldEqual "OK"
      }
    }
  }


}
