
name := "escher-akka-http"
organization  := "com.emarsys"
version       := "0.0.9"

scalaVersion := "2.11.8"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV      = "2.4.10"
  val scalaTestV = "3.0.0"
  Seq(
    "com.typesafe.akka"  %% "akka-http-core"                    % akkaV,
    "com.typesafe.akka"  %% "akka-http-experimental"            % akkaV,
    "com.typesafe.akka"  %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.akka"  %% "akka-http-testkit"                 % akkaV % "test",
    "org.scalatest"      %% "scalatest"                         % scalaTestV % "test",
    "com.emarsys"        %  "escher"                            % "0.3"
  )
}

publishTo := Some(Resolver.file("releases", new File("releases")))
