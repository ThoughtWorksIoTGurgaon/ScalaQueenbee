package src.main.scala.com.supersaiyyans.service

import com.supersaiyyans.packet.JsonCmdPacket
import play.api.libs.json.{JsObject, Json}
import src.main.scala.com.supersaiyyans.packet.WritePacket
import src.main.scala.com.supersaiyyans.service.SwitchService.SwitchServiceState


trait ServiceState

trait Service[A<: ServiceState]{
  def process(jsonCmdJsonPacket: JsonCmdPacket): WritePacket
  var state: A
  def toJson(): JsObject
}

class SwitchService(val serviceAddr: String) extends Service[SwitchServiceState]{

  var state = SwitchServiceState("OFF")
  var deviceId: String = serviceAddr.split(":")(0)
  var serviceId : Int = Integer.parseInt(serviceAddr.split(":")(1))


  def toJson(): JsObject = {
    Json.obj(
      "serviceAddress"->s"${deviceId}:${serviceId}",
    "state"->state.value,
      "serviceType"->"SWH"
    )
  }


  override def process(jsonCmdJsonPacket: JsonCmdPacket): WritePacket = {
    val serviceAddressSplit: Array[String] = jsonCmdJsonPacket
      .getServiceAddress()
      .split(":")

    jsonCmdJsonPacket.cmd.toUpperCase match {
      case "ON" =>
        updateState(SwitchServiceState("ON"))
        new WritePacket(deviceId, serviceId, Array(Tuple2(1,Array(2.toByte))))
      case "OFF" =>
        updateState(SwitchServiceState("OFF"))
        new WritePacket(deviceId, serviceId, Array(Tuple2(1,Array(1.toByte))))
    }
  }


  def updateState(state: SwitchServiceState) = {
    this.state = state
  }

}

object SwitchService {

  case class SwitchServiceState(val value: String) extends ServiceState

  object SwitchServiceState{
    implicit val SwitchServiceStateFormat = Json.format[SwitchServiceState]
  }

}
