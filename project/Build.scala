import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtStartScript
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

object PrisonerDilemmaBuild extends Build {
  val Organization = "amsterdam-scala"
  val Version      = "1.0-SNAPSHOT"
  val ScalaVersion = "2.10.3"

  lazy val prisonerDilemma = Project(
    id = "prisonerdilemma",
    base = file("."),
    settings = defaultSettings ++
      Seq(SbtStartScript.stage in Compile := Unit),
    aggregate = Seq(common, server, client)
  )

  lazy val common = Project(
    id = "common",
    base = file("common"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.prisonerdilemma)
  )

  lazy val server = Project(
    id = "server",
    base = file("server"),
    dependencies = Seq(common),
    settings = defaultSettings ++
      SbtStartScript.startScriptForClassesSettings ++
      Seq(libraryDependencies ++= Dependencies.prisonerdilemma)
  )

  lazy val client = Project(
    id = "client",
    base = file("client"),
    dependencies = Seq(common),
    settings = defaultSettings ++
      SbtStartScript.startScriptForClassesSettings ++
      Seq(libraryDependencies ++= Dependencies.prisonerdilemma)
  )

  lazy val buildSettings = Seq(
    organization := Organization,
    version      := Version,
    scalaVersion := ScalaVersion
  )

  lazy val defaultSettings = Defaults.defaultSettings ++ formatSettings ++ buildSettings ++ Seq(
    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-optimise", "-deprecation", "-unchecked"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),

    // disable parallel tests
    parallelExecution in Test := false
  )

  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test    := formattingPreferences
  )

  def formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
  }
}

object Dependencies {
  import Dependency._
  val prisonerdilemma = Seq(akkaActor, akkaCluster, scalaTest, jUnit, akkaTestKit, specs2)
}

object Dependency {
  object Version {
    val Akka      = "2.3.0"
    val Scalatest = "1.9.1"
    val JUnit     = "4.10"
    val Specs2    = "2.2.2"
  }

  // ---- Application dependencies ----

  val akkaActor   = "com.typesafe.akka"   %% "akka-actor"              % Version.Akka
  val akkaCluster = "com.typesafe.akka"   %% "akka-cluster"            % Version.Akka

  // ---- Test dependencies ----

  val scalaTest   = "org.scalatest"       %% "scalatest"               % Version.Scalatest  % "test"
  val specs2      = "org.specs2"          %% "specs2"                  % Version.Specs2     % "test"
  val akkaTestKit = "com.typesafe.akka"   %% "akka-testkit"            % Version.Akka       % "test"
  val jUnit       = "junit"               % "junit"                    % Version.JUnit      % "test"
}
