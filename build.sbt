val scalaV = "2.12.6"

name          := "escher-akka-http"
organization  := "com.emarsys"

scalaVersion  := scalaV
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaHttpV  = "10.1.9"
  val akkaStreamV = "2.5.18"
  val scalaTestV = "3.0.1"
  Seq(
    "com.typesafe.akka"  %% "akka-http-core"       % akkaHttpV,
    "com.typesafe.akka"  %% "akka-http"            % akkaHttpV,
    "com.typesafe.akka"  %% "akka-http-testkit"    % akkaHttpV  % "test",
    "com.typesafe.akka"  %% "akka-stream"          % akkaStreamV,
    "com.typesafe.akka"  %% "akka-stream-testkit"  % akkaStreamV,
    "org.scalatest"      %% "scalatest"            % scalaTestV % "test",
    "com.emarsys"        %  "escher"               % "0.3.1"
  )
}

scalaVersion in ThisBuild := scalaV

inThisBuild(List(
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/emartech/escher-akka-http")),
  developers := List(
    Developer("andrasp3a", "Andras Papp", "andras.papp@emarsys.com", url("https://github.com/andrasp3a")),
    Developer("doczir", "Robert Doczi", "doczi.r@gmail.com", url("https://github.com/doczir")),
    Developer("gyfarkas", "Gyorgy Farkas", "gyfarkas@gmail.com", url("https://github.com/gyfarkas")),
    Developer("itsdani", "Daniel Segesdi", "daniel.segesdi@emarsys.com", url("https://github.com/itsdani")),
    Developer("jupposessho", "Vilmos Feher", "vilmos.feher@emarsys.com", url("https://github.com/jupposessho")),
    Developer("Ksisu", "Kristof Horvath", "kristof.horvath@emarsys.com", url("https://github.com/Ksisu")),
    Developer("laszlovaspal", "Laszlo Vaspal", "laszlo.vaspal@emarsys.com", url("https://github.com/laszlovaspal")),
    Developer("miklos-martin", "Miklos Martin", "miklos.martin@gmail.com", url("https://github.com/miklos-martin")),
    Developer("suliatis", "Attila Suli", "attila.suli@emarsys.com", url("https://github.com/suliatis")),
    Developer("tg44", "Gergo Torcsvari", "gergo.torcsvari@emarsys.com", url("https://github.com/tg44")),
    Developer("tt0th", "Tibor Toth", "tibor.toth@emarsys.com", url("https://github.com/tt0th"))
  ),
  scmInfo := Some(ScmInfo(url("https://github.com/emartech/escher-akka-http"), "scm:git:git@github.com:emartech/escher-akka-http.git")),

  // These are the sbt-release-early settings to configure
  pgpPublicRing := file("./travis/local.pubring.asc"),
  pgpSecretRing := file("./travis/local.secring.asc"),
  releaseEarlyWith := SonatypePublisher
))
