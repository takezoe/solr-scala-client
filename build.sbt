name := "solr-scala-client"

organization := "com.github.takezoe"

version := "0.0.25"

scalaVersion := "2.12.11"

scalacOptions += "-feature"

crossScalaVersions := Seq("2.11.12", "2.12.11", "2.13.1", "3.0.1")

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "org.apache.solr"         % "solr-solrj"               % "8.4.1",
  "com.squareup.okhttp3"    % "okhttp"                   % "3.14.6",
  "org.scalatest"          %% "scalatest"                % "3.2.9" % "test",
  "org.mockito"             % "mockito-core"             % "3.3.0" % "test",
  "org.testcontainers"      % "solr"                     % "1.16.0" % "test",
  "commons-logging"         % "commons-logging"          % "1.2"   % "runtime"
)

libraryDependencies ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
  case Some((2, 11)) =>
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1"
  case Some((2, v)) if v >= 12 =>
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
  case _ =>
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.0.0"
}.toList

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
