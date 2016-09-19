package com.supersaiyyans.actors

import com.supersaiyyans.packet.Packet
import play.api.libs.json._
import com.supersaiyyans.actors.CommonMessages.{ServiceData, ServiceState, SwitchServiceState}
import com.supersaiyyans.util.Commons.AssignedServiceId

object CommonMessages {

  sealed trait State

  object AwaitingDeviceConnect extends State

  object Started extends State

  object ServiceState {
    def unapply(serviceState: ServiceState) : Option[JsValue] = {
      val (prod: Product, sub) = serviceState match {
        case s: SwitchServiceState => (s,Json.toJson(s.value))
      }
        Some(sub)
    }

    def apply(value: JsValue): ServiceState = {
      value match {
        case x: JsError => println("Error")
      }
      Json.fromJson[ServiceState](value)(JsonTranslations.Implicits.serviceStateFormat).get
    }
  }

  trait ServiceState

  case class SwitchServiceState(value: String) extends ServiceState

  case class ServiceData(name: String, assignedServiceId: AssignedServiceId, state: SwitchServiceState)

  sealed trait Command

  sealed trait NotificationCommand

  object DeviceConnected extends NotificationCommand

  class CommandPacket(packet: Packet) extends Command

}

object JsonTranslations {

  object Implicits {

    import play.api.libs.json._

    // JSON library
    import play.api.libs.json.Reads._

    // Custom validation helpers
    import play.api.libs.functional.syntax._

    // Combinator syntax

    implicit val switchServiceStateFormat = Json.format[SwitchServiceState]
    implicit val serviceStateFormat = Json.format[ServiceState]
    implicit val serviceDataFormat = Json.format[ServiceData]
  }
}
