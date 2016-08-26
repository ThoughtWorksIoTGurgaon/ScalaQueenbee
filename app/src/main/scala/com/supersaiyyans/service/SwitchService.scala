package com.supersaiyyans.service

import com.supersaiyyans.packet.{JsonCmdPacket, WritePacket}
import com.supersaiyyans.service.SwitchService.SwitchServiceState
import play.api.libs.json.{JsObject, Json}


trait ServiceState

trait Service[A <: ServiceState]{
  def processAndChangeState(jsonCmdJsonPacket: JsonCmdPacket): WritePacket
  var state: A
  implicit def toJson(): JsObject
}

case class SwitchService(serviceAddr: String,var state: SwitchServiceState) extends Service[SwitchServiceState]{

  val deviceId: String = serviceAddr.split(":")(0)
  val serviceId : Int = Integer.parseInt(serviceAddr.split(":")(1))

  def toJson(): JsObject = {
    Json.obj(
      "serviceAddress"->s"${deviceId}:${serviceId}",
    "state"->state.value,
      "serviceType"->"SWH"
    )
  }


  override def processAndChangeState(jsonCmdJsonPacket: JsonCmdPacket): WritePacket = {
    val serviceAddressSplit: Array[String] = jsonCmdJsonPacket
      .getServiceAddress
      .split(":")

    jsonCmdJsonPacket.cmd.toUpperCase match {
      case "ON" =>
        updateState(SwitchServiceState("ON"))
        new WritePacket(deviceId, serviceId, Array(Tuple2(1,Array(2.toByte))),"")
      case "OFF" =>
        updateState(SwitchServiceState("OFF"))
        new WritePacket(deviceId, serviceId, Array(Tuple2(1,Array(1.toByte))),"")
    }
  }


  def updateState(state: SwitchServiceState) = {
//    this.state = state
  }

}

object SwitchService {



  case class SwitchServiceState(val value: String) extends ServiceState

  object SwitchServiceState{
    implicit val SwitchServiceStateFormat = Json.format[SwitchServiceState]
  }

}
