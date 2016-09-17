package src.main.scala.com.supersaiyyans.actors

import com.supersaiyyans.packet.Packet

object CommonMessages {
  sealed trait State
  object AwaitingDeviceConnect extends State
  object Started extends State


  trait ServiceState

  case class ServiceData(name: String, val serviceId: String, deviceId: String, state: ServiceState)

  sealed trait Command
  sealed trait NotificationCommand
  object DeviceConnected extends NotificationCommand

  class CommandPacket(packet: Packet) extends Command
}
