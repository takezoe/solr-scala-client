name := "solr-scala-client"

organization := "com.github.takezoe"

version := "0.0.15"

scalaVersion := "2.12.0"

scalacOptions += "-feature"

resolvers += "Local Maven Repository" at "file:///" + Path.userHome.absolutePath + "/.m2/repository"

libraryDependencies ++= Seq(
  "org.apache.solr"         % "solr-solrj"               % "6.3.0"   % "compile",
  "org.asynchttpclient"     % "async-http-client"        % "2.0.24"  % "compile",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scalatest"          %% "scalatest"                % "3.0.1"   % "test",
  "org.mockito"             % "mockito-core"             % "2.2.22"  % "test",
  "commons-logging"         % "commons-logging"          % "1.2"     % "runtime"
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
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
