[![Build Status](https://travis-ci.org/emartech/escher-akka-http.svg?branch=master)](https://travis-ci.org/emartech/escher-akka-http) [![Maven Central](https://img.shields.io/maven-central/v/com.emarsys/escher-akka-http_2.12.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.emarsys%22%20AND%20a:%22escher-akka-http_2.12%22)

# escher-akka-http

## Usage

### `1.0.3` and above

Add the following to `build.sbt`:

```
libraryDependencies += "com.emarsys" %% "escher-akka-http" % "1.0.3"
```


### Prior to `1.0.3`

Add the following to `build.sbt`:

```
resolvers += "Escher repo" at "https://raw.github.com/emartech/escher-akka-http/master/releases"
```

For versions `0.0.1`, `0.0.2` and `0.0.3`:

```
libraryDependencies += "com.emarsys" % "escher-akka-http" % "0.0.3"
```

For versions above `0.0.4` (note the double `%%`):

```
libraryDependencies += "com.emarsys" %% "escher-akka-http" % "0.0.4"
```


## Example

```scala
(post & path("customers" / IntNumber / "cart")) { customerId =>
  escherAuthenticate(List("trusted-service")) { body =>
    complete(Cart(customerId, List("product1", "product2")))
  }
}
```

## Testing

Base test trait:

```scala
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
```

Test:

```scala
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
```

## Creating a release

This library is using [sbt-release-early] for releasing artifacts. Every push will be released to maven central, see the plugins documentation on the versioning schema.

### To cut a final release:

Choose the appropriate version number according to [semver] then create and push a tag with it, prefixed with `v`.
For example:

```
$ git tag -a v1.0.3
$ git push --tag
```

After pushing the tag, while it is not strictly necessary, please [draft a release on github] with this tag too.


[sbt-release-early]: https://github.com/scalacenter/sbt-release-early
[semver]: https://semver.org
[draft a release on github]: https://github.com/emartech/escher-akka-http/releases/new
