name := "solr-scala-client"

organization := "com.github.takezoe"

version := "0.0.13-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions += "-feature"

resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"

resolvers += "Local Maven Repository" at "file:///" + Path.userHome.absolutePath + "/.m2/repository"

libraryDependencies ++= Seq(
  "org.apache.solr" % "solr-solrj" % "6.1.0" % "compile",
  "com.ning" % "async-http-client" % "1.7.16" % "compile",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1",
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "org.mockito" % "mockito-core" % "1.9.0" % "test",
  "commons-logging" % "commons-logging" % "1.1.3" % "runtime"
)

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else                             Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

scalacOptions := Seq("-deprecation", "-feature")

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/takezoe/blocking-slick</url>
  <licenses>
    <license>
      <name>The Apache Software License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/takezoe/blocking-slick</url>
    <connection>scm:git:https://github.com/takezoe/blocking-slick.git</connection>
  </scm>
  <developers>
    <developer>
      <id>takezoe</id>
      <name>Naoki Takezoe</name>
    </developer>
  </developers>
)
