
val scalaV = "2.12.6"

name          := "escher-akka-http"
organization  := "com.emarsys"
version       := "0.2.3"

scalaVersion  := scalaV
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaHttpV  = "10.0.11"
  val scalaTestV = "3.0.1"
  Seq(
    "com.typesafe.akka"  %% "akka-http-core"       % akkaHttpV,
    "com.typesafe.akka"  %% "akka-http"            % akkaHttpV,
    "com.typesafe.akka"  %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka"  %% "akka-http-testkit"    % akkaHttpV  % "test",
    "org.scalatest"      %% "scalatest"            % scalaTestV % "test",
    "com.emarsys"        %  "escher"               % "0.3.1"
  )
}

scalaVersion in ThisBuild := scalaV

publishTo := Some(Resolver.file("releases", new File("releases")))
