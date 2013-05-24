package jp.sf.amateras.solr.scala

import scala.reflect.NameTransformer

/**
 * Provides conversion methods for Map and case class.
 */
private[scala] object CaseClassMapper {

  /**
   * Converts the given Map[String, Any] to the case class.
   *
   */
  def map2class[T](map: Map[String, Any])(implicit m: scala.reflect.Manifest[T]): T = {
    val clazz = m.erasure.asInstanceOf[Class[T]]

    val constructor = clazz.getConstructors()(0)
    val paramTypes = constructor.getParameterTypes()
    val params = paramTypes.map { getDefaultValue(_).asInstanceOf[java.lang.Object] }

    val instance = constructor.newInstance(params: _*).asInstanceOf[T]

    clazz.getDeclaredFields.foreach { field =>
      val value = map.get(NameTransformer.decode(field.getName)).orNull
      if(field != null){
        field.setAccessible(true)
        if(field.getType() == classOf[Option[_]]){
          field.set(instance, Option(value))
        } else {
          field.set(instance, value)
        }
      }
    }

    instance
  }

  /**
   * Converts the case class to the Map[String, Any].
   *
   */
  def class2map(instance: Any): Map[String, Any] = {
    val fields = instance.getClass().getDeclaredFields()
    fields.map { field =>
      field.setAccessible(true)
      field.get(instance) match {
        case Some(x) => (NameTransformer.decode(field.getName()), x)
        case None    => (NameTransformer.decode(field.getName()), null)
        case x       => (NameTransformer.decode(field.getName()), x)
      }
    }.toMap
  }

  def toMap(obj: Any): Map[String, Any] = {
    obj match {
      case null => Map[String, Any]()
      case map: Map[_, _] => map.asInstanceOf[Map[String, Any]]
      case x => class2map(x)
    }
  }

  def toMapArray(objs: Any*): Array[Map[String, Any]] = {
    objs.map { obj => toMap(obj) }.toArray
  }

  /**
   * Returns the default value for the given type.
   */
  private def getDefaultValue(clazz: Class[_]): Any = {
    if(clazz == classOf[Int] || clazz == java.lang.Integer.TYPE ||
        clazz == classOf[Short] || clazz == java.lang.Short.TYPE ||
        clazz == classOf[Byte] || clazz == java.lang.Byte.TYPE){
      0
    } else if(clazz == classOf[Double] || clazz == java.lang.Double.TYPE){
      0d
    } else if(clazz == classOf[Float] || clazz == java.lang.Float.TYPE){
      0f
    } else if(clazz == classOf[Long] || clazz == java.lang.Long.TYPE){
      0l
    } else if(clazz == classOf[Char] || clazz == java.lang.Character.TYPE){
      '\0'
    } else if(clazz == classOf[Boolean] || clazz == java.lang.Boolean.TYPE){
      false
    } else {
      null
    }
  }

}
