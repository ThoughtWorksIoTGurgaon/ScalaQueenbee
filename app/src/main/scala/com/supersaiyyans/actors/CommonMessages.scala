package src.main.scala.com.supersaiyyans.actors

import com.supersaiyyans.packet.Packet
import play.api.libs.json._
import src.main.scala.com.supersaiyyans.actors.CommonMessages.{ServiceData, ServiceState, SwitchServiceState}

object CommonMessages {

  sealed trait State

  object AwaitingDeviceConnect extends State

  object Started extends State

  object ServiceState {
    def unapply(serviceState: ServiceState) : Option[JsValue] = {
      val (prod: Product, sub) = serviceState match {
        case s: SwitchServiceState => (s,Json.toJson(s)(JsonTranslations.Implicits.switchServiceStateFormat))
      }
        Some(sub)
    }

    def apply(data: JsValue): ServiceState = {
      Json.fromJson[SwitchServiceState](data)(JsonTranslations.Implicits.switchServiceStateFormat).get
    }
  }

  trait ServiceState

  case class SwitchServiceState(value: String) extends ServiceState
  
  case class ServiceData(name: String, val serviceId: String, deviceId: String, state: ServiceState)

  sealed trait Command

  sealed trait NotificationCommand

  object DeviceConnected extends NotificationCommand

  class CommandPacket(packet: Packet) extends Command

}

object JsonTranslations {

  object Implicits {

    implicit val switchServiceStateFormat = Json.format[SwitchServiceState]
    implicit val serviceStateFormat = Json.format[ServiceState]
    implicit val serviceDataFormat = Json.format[ServiceData]
  }

}
