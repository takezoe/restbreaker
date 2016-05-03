package io.gitbucket.restbreaker

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser, Version}
import scala.reflect.ClassTag

object JsonUtils {

  private val mapper = new ObjectMapper()
  mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.registerModule(DefaultScalaModule)
//  mapper.registerModule(new SimpleModule("MyModule", Version.unknownVersion())
//    .addSerializer(classOf[DateTime], new JsonSerializer[DateTime] {
//      override def serialize(value: DateTime, generator: JsonGenerator, provider: SerializerProvider): Unit = {
//        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZoneUTC()
//        generator.writeString(formatter.print(value))
//      }
//    })
//    .addDeserializer(classOf[DateTime], new JsonDeserializer[DateTime](){
//      override def deserialize(parser: JsonParser, context: DeserializationContext): DateTime = {
//        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZoneUTC()
//        formatter.parseDateTime(if(parser.getValueAsString != null) parser.getValueAsString else parser.nextTextValue)
//      }
//    })
//  )

  def serialize(doc: AnyRef): String = mapper.writeValueAsString(doc)

  def deserialize[T](json: String)(implicit c: ClassTag[T]): T = mapper.readValue(json, c.runtimeClass).asInstanceOf[T]

}