package com.codahale.jerkson.ser

import java.lang.reflect.Modifier
import com.codahale.jerkson.JsonSnakeCase
import com.codahale.jerkson.Util._
import org.codehaus.jackson.JsonGenerator
import org.codehaus.jackson.annotate.{JsonIgnore, JsonIgnoreProperties}
import org.codehaus.jackson.map.{SerializerProvider, JsonSerializer}
import org.codehaus.jackson.map.annotate.JsonCachable

@JsonCachable
class CaseClassSerializer[A <: Product](klass: Class[_]) extends JsonSerializer[A] {
  /**
   * Find all annotations on this class or anything it inherits from
   * FIXME - there has to be a generic way to pass in the annotation type.
   */
  private def getAnnotations(klass: java.lang.Class[_]): Seq[JsonIgnoreProperties] = {
    if (klass == null)
      List.empty[JsonIgnoreProperties]
    else
      getAnnotations(klass.getSuperclass) ++ Option(klass.getAnnotation(classOf[JsonIgnoreProperties])).toList
  }

  private val isSnakeCase = klass.isAnnotationPresent(classOf[JsonSnakeCase])
  private val ignoredFields: Set[String] = {
    getAnnotations(klass).toSet.flatMap { a: JsonIgnoreProperties =>
      a.value.toSet
    }
  }
  
  private val nonIgnoredFields = klass.getDeclaredFields.filterNot { f =>
    f.getAnnotation(classOf[JsonIgnore]) != null ||
    ignoredFields(f.getName) ||
      (f.getModifiers & Modifier.TRANSIENT) != 0 ||
      f.getName.contains("$")
  }

  private val methods = klass.getDeclaredMethods
                                .filter { _.getParameterTypes.isEmpty }
                                .map { m => m.getName -> m }.toMap
  
  def serialize(value: A, json: JsonGenerator, provider: SerializerProvider) {
    json.writeStartObject()
    for (field <- nonIgnoredFields) {
      val methodOpt = methods.get(field.getName)
      val fieldValue: Object = methodOpt.map { _.invoke(value) }.getOrElse(field.get(value))
      if (fieldValue != None) {
        val fieldName = methodOpt.map { _.getName }.getOrElse(field.getName)
        provider.defaultSerializeField(if (isSnakeCase) snakeCase(fieldName) else fieldName, fieldValue, json)
      }
    }
    json.writeEndObject()
  }
}
