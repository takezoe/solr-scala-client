package com.github.takezoe.solr.scala

import org.scalatest.funsuite.AnyFunSuite

class CaseClassMapperSuite extends AnyFunSuite {

  test("map2class (Option is None)"){
    val employee = CaseClassMapper.map2class[Employee](Map("id" -> 1234, "name" -> "takezoe"))

    assert(employee.id == 1234)
    assert(employee.name == "takezoe")
    assert(employee.email == None)
  }

  test("map2class (Option is Some)"){
    val employee = CaseClassMapper.map2class[Employee](Map("id" -> 1234, "name" -> "takezoe", "email" -> "takezoe@gmail.com"))

    assert(employee.id == 1234)
    assert(employee.name == "takezoe")
    assert(employee.email == Some("takezoe@gmail.com"))
  }

  test("map2class (property name contains '-')"){
    val employee = CaseClassMapper.map2class[Employee2](Map("emp-id" -> 1234, "emp-name" -> "takezoe", "email" -> "takezoe@gmail.com"))

    assert(employee.`emp-id` == 1234)
    assert(employee.`emp-name` == "takezoe")
    assert(employee.email == Some("takezoe@gmail.com"))
  }
  
  test("class2map (Option is None)"){
    val map = CaseClassMapper.class2map(Employee(5678, "takezoe", None))

    assert(map("id") == 5678)
    assert(map("name") == "takezoe")
    assert(map("email") == null)
  }

  test("class2map (Option is Some)"){
    val map = CaseClassMapper.class2map(Employee(5678, "takezoe", Some("takezoe@gmail.com")))

    assert(map("id") == 5678)
    assert(map("name") == "takezoe")
    assert(map("email") == "takezoe@gmail.com")
  }

  test("class2map (property name contains '-')"){
    val map = CaseClassMapper.class2map(Employee2(5678, "takezoe", Some("takezoe@gmail.com")))

    assert(map("emp-id") == 5678)
    assert(map("emp-name") == "takezoe")
    assert(map("email") == "takezoe@gmail.com")
  }
}

case class Employee(id: Int, name: String, email: Option[String])

case class Employee2(`emp-id`: Int, `emp-name`: String, email: Option[String])
