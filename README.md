[![Build Status](https://travis-ci.org/emartech/escher-akka-http.svg?branch=master)](https://travis-ci.org/emartech/escher-akka-http)

escher-akka-http
================

Usage
-----

Add the following to `build.sbt`:

    resolvers += "Escher repo" at "https://raw.github.com/emartech/escher-akka-http/master/releases"

For versions `0.0.1`, `0.0.2` and `0.0.3`:

    libraryDependencies += "com.emarsys" % "escher-akka-http" % "0.0.3"

For versions above `0.0.4` (note the double `%%`):

    libraryDependencies += "com.emarsys" %% "escher-akka-http" % "0.0.4"


Example
-------

    (post & path("customers" / IntNumber / "cart")) { customerId =>
      escherAuthenticate(List("trusted-service")) { body =>
        complete(Cart(customerId, List("product1", "product2")))
      }
    }

Testing
-------

Base test trait:

    trait ServiceTestBase
      extends WordSpec
        with Matchers
        with ScalatestRouteTest
        with DefaultJsonProtocol
        with FamilyFormats
        with Config
        with ScalaFutures
        with EscherDirectives {

      def signed(r: HttpRequest, intervalMillis: Int = 15): HttpRequest =
        signRequest("trusted-service")(executor, materializer)(r).futureValue(timeout(1.second), interval(intervalMillis.millis))
    }

Test:

    class CartServiceSpec extends ServiceTestBase with CartService {
      override def testConfigSource = "akka.loglevel = WARNING"

      val customerId = 123

      "POST cart" should {

        trait CreateScope {
          val uri = s"http://${escherConfig.hostName}:${escherConfig.port}/customers/$customerId/cart"
        }

        "return created cart" in new CreateScope {
          val originalId = 101
          signed(Post(uri, Cart(customerId, List("product1", "product2"))), 100) ~> routes ~> check {
            status shouldBe OK
            responseAs[Cart] shouldEqual Cart(customerId, List("product1", "product2"))
          }
        }
      }

    }

Creating a release
------------------

Bump the version number in `build.sbt` and run the following command:

    sbt publish

This will build a jar with the new version and place it under the `releases` directory.
