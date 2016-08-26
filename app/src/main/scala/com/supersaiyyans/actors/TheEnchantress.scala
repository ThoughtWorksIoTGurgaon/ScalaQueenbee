package com.supersaiyyans.actors

import akka.actor.{Terminated, ActorRef, FSM, Props}
import com.supersaiyyans.actors.ServiceActors.SwitchServiceData
import com.supersaiyyans.actors.TheEnchantress._
import com.supersaiyyans.packet.Packet
import com.supersaiyyans.util.Logger._
import src.main.scala.com.supersaiyyans.actors.MQTTDiscoveryActor


/*
TODO: Support multi service discovery
 */
class TheEnchantress(repoActor: ActorRef) extends FSM[State, Data] {

  context watch repoActor
  val mqttDiscoveryActor = context.actorOf(MQTTDiscoveryActor.props)
  startWith(Processing, EnchantressData(List.empty[ActorRef]))

  when(Processing) {
    case Event(service: ServiceDiscovered, data: EnchantressData) =>
      val switchServiceActor =
        context
          .actorOf(
            SwitchServiceActor
              .props(service.deviceId, service.serviceId, SwitchServiceData("OFF"), repoActor))
      stay using EnchantressData(data.serviceActors :+ switchServiceActor)

    case Event(Terminated(message),data) =>
      debug(s"RepoActor is down,enchantress wont work properly: ${message}")
      stay
  }


}

object TheEnchantress {

  sealed trait State

  object Processing extends State

  sealed trait Data

  sealed case class EnchantressData(serviceActors: List[ActorRef]) extends Data

  case class ServiceDiscovered(deviceId: String, serviceId: String, profileId: String)


  case class ProcessPacketFromUser(packet: Packet)

  case class ProcessSystemPacket(packet: Packet)

  def props(repoActor: ActorRef) = Props(new TheEnchantress(repoActor))

  trait ServiceState

  case class SwitchServiceState(value: String)

  case class ServiceData(name: String, val serviceId: String, deviceId: String, state: ServiceState)


}
