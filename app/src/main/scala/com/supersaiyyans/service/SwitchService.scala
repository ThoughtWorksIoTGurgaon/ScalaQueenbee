package src.main.scala.com.supersaiyyans.service

import com.supersaiyyans.packet.JsonCmdPacket
import src.main.scala.com.supersaiyyans.packet.WritePacket

trait Service{
  def process(jsonCmdJsonPacket: JsonCmdPacket): WritePacket
}

class SwitchService extends Service{
  override def process(jsonCmdJsonPacket: JsonCmdPacket): WritePacket = {
    val serviceAddressSplit: Array[String] = jsonCmdJsonPacket
      .getServiceAddress()
      .split(":")

    val deviceId = serviceAddressSplit(0)
    val serviceId = serviceAddressSplit(1).toInt

    jsonCmdJsonPacket.cmd match {
      case "ON" =>
        new WritePacket(deviceId, serviceId, Array(Tuple2(1,Array(1.toByte))))
      case "OFF" =>
        new WritePacket(deviceId, serviceId, Array(Tuple2(1,Array(2.toByte))))
    }
  }
}
