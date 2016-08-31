package com.supersaiyyans.actors

import akka.actor.{ActorRef, FSM, Props}
import com.supersaiyyans.actors.ServiceActors._
import com.supersaiyyans.packet._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import com.supersaiyyans.util.Logger._



//TODO: FIX IMPORTS and fix message container fuckups!
object PacketTransformers {


  implicit class IJsonTransformer(jsonPacket: JsonCmdPacket) {

    def transformToBinary(serviceId: String, deviceId: String)(implicit binaryTransformer: JsonCmdPacket => BinaryPacket) = {
      binaryTransformer(jsonPacket)
    }
  }

  //  def transformJsonToBinaryPacket : JsonCmdPacket => WritePacket = {
  //    jsonPacket=>
  //      jsonPacket.cmd.toUpperCase match {
  //        case "ON" => new WritePacket(deviceId, serviceId, Array(Tuple2(1,Array(2.toByte))))
  //      }
  //  }
}



class SwitchServiceActor(deviceId: String, serviceId: String, switchData: SwitchServiceData, override val serviceRepoActor: ActorRef)
  extends ServiceActor {

  val mqttActorRef = context

  startWith(AwaitingDeviceConnect, switchData)

  context.system.scheduler.scheduleOnce(2 minutes,self,TryConnect)
  debug("")


  when(AwaitingDeviceConnect) {
    case Event(DeviceConnected, _) =>
      goto(Started)
    case Event(TryConnect, _) =>
      context.system.scheduler.scheduleOnce(2 minutes,self,TryConnect)
      stay
  }

  when(Started) {
    case Event(cmdPacket: CommandPacket, _) =>
      cmdPacket.packet match {
        case jsonPacket: JsonCmdPacket =>
          jsonPacket.cmd
      }
      goto(Started)
  }

}

object SwitchServiceActor {

  def props(deviceId: String, serviceId: String, switchData: SwitchServiceData, serviceRepoActor: ActorRef) = {
    Props(new SwitchServiceActor(deviceId, serviceId, switchData, serviceRepoActor))
  }
}

trait ServiceActor extends FSM[State, Data] with RetryConnect{
  def serviceRepoActor: ActorRef
}


object ServiceActors {

  sealed trait State

  object AwaitingDeviceConnect extends State

  object Started extends State

  sealed trait Data

  sealed trait ServiceData extends Data

  case class SwitchServiceData(val value: String) extends ServiceData

  sealed trait Command

  sealed trait NotificationCommand

  object DeviceConnected extends NotificationCommand

  class CommandPacket(val packet: JsonCmdPacket) extends Command

}
