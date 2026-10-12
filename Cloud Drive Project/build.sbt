ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.8"

lazy val root = (project in file("."))
  .settings(
    name := "Cloud Drive Project"
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test