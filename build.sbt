name := "solr-scala-client"

organization := "com.github.takezoe"

version := "0.0.14-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions += "-feature"

resolvers += "Local Maven Repository" at "file:///" + Path.userHome.absolutePath + "/.m2/repository"

libraryDependencies ++= Seq(
  "org.apache.solr"         % "solr-solrj"               % "6.1.0"   % "compile",
  "org.asynchttpclient"     % "async-http-client"        % "2.0.11"  % "compile",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scalatest"          %% "scalatest"                % "3.0.0"   % "test",
  "org.mockito"             % "mockito-core"             % "1.10.19" % "test",
  "commons-logging"         % "commons-logging"          % "1.2"     % "runtime"
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
  <url>https://github.com/takezoe/solr-scala-client</url>
  <licenses>
    <license>
      <name>The Apache Software License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/takezoe/solr-scala-client</url>
    <connection>scm:git:https://github.com/takezoe/solr-scala-client.git</connection>
  </scm>
  <developers>
    <developer>
      <id>takezoe</id>
      <name>Naoki Takezoe</name>
    </developer>
  </developers>
)
