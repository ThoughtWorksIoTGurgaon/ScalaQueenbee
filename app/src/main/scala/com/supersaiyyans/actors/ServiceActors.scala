package com.supersaiyyans.actors

import akka.actor.{Actor, ActorRef, FSM, Props}
import com.supersaiyyans.actors.ServiceActors.SupportedProtocolTypes.{MQTT, ProtocolType}
import com.supersaiyyans.actors.ServiceActors._
import com.supersaiyyans.packet._
import com.supersaiyyans.util.Logger._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._


//TODO: FIX IMPORTS and fix message container fuckups!
object PacketTransformers {


  implicit class IJsonTransformer(jsonPacket: JsonCmdPacket) {

    def transformToBinary(serviceId: String, deviceId: String)(implicit binaryTransformer: JsonCmdPacket => BinaryPacket) = {
      binaryTransformer(jsonPacket)
    }
  }

}


class SwitchServiceActor(override val deviceId: String, override val serviceId: String, switchData: SwitchServiceData, override val serviceRepoActor: ActorRef, override val protocolType: ProtocolType)
  extends ServiceActor {

  val mqttActorRef = context.actorOf(ProtocolActorDecider(protocolType))

  startWith(Started, switchData)



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

  def props(deviceId: String, serviceId: String, switchData: SwitchServiceData, serviceRepoActor: ActorRef, protocolType: ProtocolType) = {
    Props(new SwitchServiceActor(deviceId, serviceId, switchData, serviceRepoActor, protocolType))
  }
}


trait ServiceActor extends FSM[State, Data] with RetryConnect {
  val serviceRepoActor: ActorRef
  val protocolType: ProtocolType
  val deviceId: String
  val serviceId: String

  def ProtocolActorDecider: (ProtocolType) =>  Props = {
    protocol =>
      protocol match {
        case _ =>
          Props(new MQTTPubSubProxySupervisorImpl(self,deviceId))
      }
  }
}


object ServiceActors {

  sealed trait State

  object Started extends State

  sealed trait Data

  sealed trait ServiceData extends Data

  case class SwitchServiceData(val value: String) extends ServiceData

  class CommandPacket(val packet: JsonCmdPacket) extends Command

  trait ProtocolDescriber {
    this: Actor =>
    val myProtocol: ProtocolType
  }

  trait MQTTActor {
    this: ProtocolDescriber =>
    val myProtocol = MQTT

  }

  object SupportedProtocolTypes {
    trait ProtocolType
    case object MQTT extends ProtocolType

  }


}
