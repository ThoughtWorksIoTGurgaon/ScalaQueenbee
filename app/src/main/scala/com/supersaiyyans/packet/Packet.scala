package com.supersaiyyans.packet

import play.api.libs.json.{Format, Json}

trait Packet


case class WritePacket(val cmd: String) extends Packet{

}

object WritePacket{
  implicit val writePacketFormat: Format[WritePacket] = Json.format[WritePacket]
}
