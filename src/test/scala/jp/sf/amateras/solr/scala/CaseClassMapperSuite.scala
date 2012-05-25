package jp.sf.amateras.solr.scala
import org.scalatest.FunSuite
import jp.sf.amateras.solr.scala.sample.CaseClassMapper

class CaseClassMapperSuite extends FunSuite {


  test("map2class"){
    val employee = CaseClassMapper.map2class[Employee](Map("id" -> 1234, "name" -> "takezoe"))

    assert(employee.id == 1234)
    assert(employee.name == "takezoe")
  }

  test("class2map"){
    val map = CaseClassMapper.class2map(Employee(5678, "takezoe"))

    assert(map("id") == 5678)
    assert(map("name") == "takezoe")
  }

}

case class Employee(id: Int, name: String)
