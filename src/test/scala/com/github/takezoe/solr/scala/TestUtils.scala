package com.github.takezoe.solr.scala

import org.scalatest._
import org.scalactic.source
import org.scalatest.funsuite.AnyFunSuite

object TestUtils {
  val scalaVersion = buildinfo.BuildInfo.scalaVersion

  def withScalaVersion(name: String): String = {
    s"${name} - ${scalaVersion}"
  }

  implicit class TestNameString(name: String) {
    def withScalaVersion: String = TestUtils.withScalaVersion(name)
  }
}
