name := "solr-scala-client"

organization := "jp.sf.amateras.solr.scala"

version := "0.0.6"

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.1", "2.9.1-1", "2.9.2", "2.10.0")

resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"

resolvers += "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
  "org.apache.solr" % "solr-solrj" % "3.5.0" % "compile",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.mockito" % "mockito-core" % "1.9.0" % "test"
)

publishTo := Some(Resolver.ssh("amateras-repo-scp", "shell.sourceforge.jp", "/home/groups/a/am/amateras/htdocs/mvn/") withPermissions("0664")
  as(System.getProperty("user.name"), new java.io.File(Path.userHome.absolutePath + "/.ssh/id_rsa")))

EclipseKeys.withSource := true