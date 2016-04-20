package com.experiments.calculator.serialization

import java.io.ByteArrayOutputStream

import akka.serialization.SerializerWithStringManifest
import com.experiments.calculator._
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream}

class AvroSpecificSerializer extends SerializerWithStringManifest {
  final val AddedManifest = classOf[Added].getName
  final val SubtractedManifest = classOf[Subtracted].getName
  final val MultipliedManifest = classOf[Multiplied].getName
  final val DividedManifest = classOf[Divided].getName
  final val ResetManifest = classOf[Reset].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def identifier: Int = 9002

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case AddedManifest =>
      val in = AvroInputStream[Added](bytes)
      in.iterator.toList.head
    case SubtractedManifest =>
      val in = AvroInputStream[Subtracted](bytes)
      in.iterator.toList.head
    case MultipliedManifest =>
      val in = AvroInputStream[Multiplied](bytes)
      in.iterator.toList.head
    case DividedManifest =>
      val in = AvroInputStream[Divided](bytes)
      in.iterator.toList.head
    case ResetManifest =>
      val in = AvroInputStream[Reset](bytes)
      in.iterator.toList.head
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case a: Added =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream[Added](os = baos, includeSchema = true)
      output.write(a)
      output.close()
      baos.toByteArray

    case s: Subtracted =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream[Subtracted](os = baos, includeSchema = true)
      output.write(s)
      output.close()
      baos.toByteArray

    case m: Multiplied =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream[Multiplied](os = baos, includeSchema = true)
      output.write(m)
      output.close()
      baos.toByteArray

    case d: Divided =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream[Divided](os = baos, includeSchema = true)
      output.write(d)
      output.close()
      baos.toByteArray

    case r: Reset =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream[Reset](os = baos, includeSchema = true)
      output.write(r)
      output.close()
      baos.toByteArray
  }
}
