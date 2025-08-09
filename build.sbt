val scala3Version = "3.7.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "valmap",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    // note: use single percent for pdfbox since it is a java library not a scala library

    //libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
    libraryDependencies += "org.scala-lang" %% "toolkit" % "0.2.1",
    libraryDependencies += "org.scala-lang" %% "toolkit-test" % "0.2.1" % Test,
    //libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.12.0",
    libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "3.0.5"
  )
