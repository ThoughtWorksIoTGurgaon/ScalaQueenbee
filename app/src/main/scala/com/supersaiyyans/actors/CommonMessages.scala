package src.main.scala.com.supersaiyyans.actors

import com.supersaiyyans.packet.Packet

object CommonMessages {
  sealed trait State
  object AwaitingDeviceConnect extends State
  object Started extends State

  sealed trait Data
  sealed trait ServiceData extends Data
  case class SwitchServiceData(val value: String) extends ServiceData

  sealed trait Command
  sealed trait NotificationCommand
  object DeviceConnected extends NotificationCommand

  class CommandPacket(packet: Packet) extends Command
}
