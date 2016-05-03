name := "restbreaker"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.asynchttpclient" % "async-http-client" % "2.0.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.5.3"
)
