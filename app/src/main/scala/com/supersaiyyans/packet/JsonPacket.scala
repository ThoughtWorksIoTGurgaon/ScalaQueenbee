package com.supersaiyyans.packet

import play.api.libs.json.{Format, Json}

trait JsonPacket extends Packet{
}

trait Command


case class JsonCmdPacket(val serviceAddress: String, val cmd: String) extends JsonPacket {

  val toByteData : Vector[Byte] = {
    Vector(cmd.toByte)
  }

  override val getServiceAddress: String = serviceAddress

  override val getSourceAddress: String = ???
  override val getDestinationAddress: String = ???
}

object JsonCmdPacket {
  implicit val writePacketFormat: Format[JsonCmdPacket] = Json.format[JsonCmdPacket]
}
