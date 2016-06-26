name := "restbreaker"

organization := "com.github.takezoe"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.asynchttpclient" % "async-http-client" % "2.0.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.5.3",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.eclipse.jetty.aggregate" % "jetty-all" % "9.1.4.v20140401" % "test"
)

parallelExecution in Test := false

publishTo <<= (version) { version: String =>
  val repoInfo =
    if (version.trim.endsWith("SNAPSHOT"))
      ("amateras snapshots" -> "/home/groups/a/am/amateras/htdocs/mvn-snapshot/")
    else
      ("amateras releases" -> "/home/groups/a/am/amateras/htdocs/mvn/")
  Some(Resolver.ssh(
    repoInfo._1,
    "shell.sourceforge.jp",
    repoInfo._2) as(System.getProperty("user.name"), (Path.userHome / ".ssh" / "id_rsa").asFile) withPermissions("0664"))
}
