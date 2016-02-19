package com.supersaiyyans.packet

import play.api.libs.json.{Format, Json}

trait JsonPacket {
  def toByteData(): Vector[Byte]

  def getServiceAddress(): String
}


case class JsonCmdPacket(val serviceAddress: String, val cmd: String) extends JsonPacket {
  def toByteData(): Vector[Byte] = {
    return Vector(cmd.toByte)
  }

  override def getServiceAddress(): String = serviceAddress
}

object JsonCmdPacket {
  implicit val writePacketFormat: Format[JsonCmdPacket] = Json.format[JsonCmdPacket]
}
