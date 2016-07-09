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
      val in = AvroInputStream.data[Added](bytes)
      in.iterator.toList.head
    case SubtractedManifest =>
      val in = AvroInputStream.data[Subtracted](bytes)
      in.iterator.toList.head
    case MultipliedManifest =>
      val in = AvroInputStream.data[Multiplied](bytes)
      in.iterator.toList.head
    case DividedManifest =>
      val in = AvroInputStream.data[Divided](bytes)
      in.iterator.toList.head
    case ResetManifest =>
      val in = AvroInputStream.data[Reset](bytes)
      in.iterator.toList.head
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case a: Added =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream.binary[Added](baos)
      output.write(a)
      output.close()
      baos.toByteArray

    case s: Subtracted =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream.binary[Subtracted](baos)
      output.write(s)
      output.close()
      baos.toByteArray

    case m: Multiplied =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream.binary[Multiplied](baos)
      output.write(m)
      output.close()
      baos.toByteArray

    case d: Divided =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream.binary[Divided](baos)
      output.write(d)
      output.close()
      baos.toByteArray

    case r: Reset =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream.binary[Reset](baos)
      output.write(r)
      output.close()
      baos.toByteArray
  }
}
