package dsentric.schema

import dsentric.{DArray, DObject, ForceWrapper}

import scala.collection.mutable

object JsonSchema {

  def convertObjectDefinition(objectDefinition:ObjectDefinition):DObject =
    ForceWrapper.dObject(convertTypeDefinition(objectDefinition).toMap)

  def convertObjectDefinitions(objectDefinitions:Vector[ObjectDefinition]):DArray =
    ForceWrapper.dArray(objectDefinitions.map(convertTypeDefinition(_).toMap))


  private def convertPropertyDefinition(propertyDefinition: PropertyDefinition):(String, Map[String, Any]) = {
    val m = convertTypeDefinition(propertyDefinition.typeDefinition)
    propertyDefinition.description.foreach(p => m += "description" -> p)
    propertyDefinition.examples.foreach(p => m += "example" -> p)
    propertyDefinition.key -> m.toMap
  }

  private def convertTypeDefinition(typeDefinition: TypeDefinition):mutable.HashMap[String, Any] = {
    val m = new mutable.HashMap[String, Any]()
    m += "type" -> typeDefinition.name
    if (typeDefinition.enum.nonEmpty) m += "enum" -> typeDefinition.enum.toVector
    typeDefinition match {
      case r:ByRefDefinition =>
        m.clear()
        m += "$ref" -> s"#/definitions/${r.name}"
      case s:StringDefinition =>
        s.pattern.foreach(p => m += "pattern" -> p)
        s.format.foreach(p => m += "format" -> p)
        s.maxLength.foreach(p => m += "maxLength" -> p)
        s.minLength.foreach(p => m += "minLength" -> p)
      case n:NumberDefinition =>
        n.exclusiveMaximum.foreach(p => m + "exclusiveMaximum" -> p)
        n.maximum.foreach(p => m + "maximum" -> p)
        n.exclusiveMinimum.foreach(p => m + "exclusiveMinimum" -> p)
        n.minimum.foreach(p => m + "minimum" -> p)
        n.multipleOf.foreach(p => m + "multipleOf" -> p)
      case n:IntegerDefinition =>
        n.exclusiveMaximum.foreach(p => m + "exclusiveMaximum" -> p)
        n.maximum.foreach(p => m + "maximum" -> p)
        n.exclusiveMinimum.foreach(p => m + "exclusiveMinimum" -> p)
        n.minimum.foreach(p => m + "minimum" -> p)
        n.multipleOf.foreach(p => m + "multipleOf" -> p)
      case a:ArrayDefinition =>
        a.minLength.foreach(p => m + "minLength" -> p)
        a.maxLength.foreach(p => m += "maxLength" -> p)
        if (a.uniqueness) m += "uniqueness" -> true
        a.items.foreach(p => m += "items" -> convertTypeDefinition(p).toMap)
      case o:ObjectDefinition if o.referencedDefinitions.isEmpty =>
        if (o.properties.nonEmpty) m += "properties" -> o.properties.map(convertPropertyDefinition).toMap
        val required = o.properties.collect{ case p if p.required => p.key}
        if (required.nonEmpty) m += "required" -> required
        o.title.foreach(p => m += "title" -> p)
        o.description.foreach(p => m += "description" -> p)
        o.minProperties.foreach(p => m += "minProperties" -> p)
        o.maxProperties.foreach(p => m += "maxProperties" -> p)
        m += "additionalProperties" -> o.additionalProperties
        o.propertyNames.foreach(p => m += "propertyNames" -> p)
        if (o.patternProperties.nonEmpty) m += "patternProperties" -> o.patternProperties.mapValues(convertTypeDefinition(_).toMap)
      case o:ObjectDefinition =>
        val removedObj = o.copy(referencedDefinitions = Vector.empty)
        val obj =
          if (removedObj == ObjectDefinition.empty) Vector.empty
          else Vector(convertTypeDefinition(o).toMap)
        val allOf =
          o.referencedDefinitions.map(s => Map("$ref" -> s"#/definitions/$s")) ++ obj
        m += "allOf" -> allOf
    }
    m
  }
}
