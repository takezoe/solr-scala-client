package jp.sf.amateras.solr.scala
import org.scalatest.FunSuite

class CaseClassMapperSuite extends FunSuite {

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

}

case class Employee(id: Int, name: String, email: Option[String])
