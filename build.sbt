name := "solr-scala-client"

organization := "jp.sf.amateras.solr.scala"

version := "0.0.13-SNAPSHOT"

//crossScalaVersions := Seq("2.10.3", "2.11.1")
scalaVersion := "2.11.7"

scalacOptions += "-feature"

resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"

resolvers += "Local Maven Repository" at "file:///" + Path.userHome.absolutePath + "/.m2/repository"

libraryDependencies ++= Seq(
  "org.apache.solr" % "solr-solrj" % "4.5.1" % "compile",
  "com.ning" % "async-http-client" % "1.7.16" % "compile",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1",
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "org.mockito" % "mockito-core" % "1.9.0" % "test",
  "commons-logging" % "commons-logging" % "1.1.3" % "runtime"
)

// libraryDependencies := {
//   CrossVersion.partialVersion(scalaVersion.value) match {
//     case Some((2, scalaMajor)) if scalaMajor >= 11 =>
//       libraryDependencies.value ++ Seq(
//         "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
//       )
//     case _ => libraryDependencies.value
//   }
// }

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
