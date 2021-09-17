organization  := "com.emarsys"
name          := "escher-akka-http"
crossScalaVersions := List("3.0.2", "2.13.6", "2.12.15")

scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaStreamV = "2.6.16"
  val akkaHttpV  = "10.2.6"
  val scalaTestV = "3.2.10"
  Seq(
    ("com.typesafe.akka"  %% "akka-http-core"       % akkaHttpV).cross(CrossVersion.for3Use2_13),
    ("com.typesafe.akka"  %% "akka-http"            % akkaHttpV).cross(CrossVersion.for3Use2_13),
    ("com.typesafe.akka"  %% "akka-http-testkit"    % akkaHttpV  % Test).cross(CrossVersion.for3Use2_13),
    ("com.typesafe.akka"  %% "akka-stream"          % akkaStreamV).cross(CrossVersion.for3Use2_13),
    ("com.typesafe.akka"  %% "akka-stream-testkit"  % akkaStreamV % Test).cross(CrossVersion.for3Use2_13),
    "org.scalatest"      %% "scalatest"            % scalaTestV % Test,
    "com.emarsys"        %  "escher"               % "0.3.5",
    "org.scala-lang.modules" %% "scala-collection-compat" % "2.5.0"
  )
}

Global / onChangedBuildSource := ReloadOnSourceChanges

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
  )
))
