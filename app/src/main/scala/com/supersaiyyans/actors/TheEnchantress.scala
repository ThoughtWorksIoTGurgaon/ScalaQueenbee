package com.supersaiyyans.actors

import java.util.UUID

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import com.supersaiyyans.actors.MQTTDiscoveryActor.WhichProtocol
import com.supersaiyyans.actors.ProfileType.ProfileType
import com.supersaiyyans.actors.ServiceActors.SupportedChannelTypes.ChannelType
import com.supersaiyyans.actors.ServicesRepoActor.AddService
import com.supersaiyyans.actors.TheEnchantress._
import com.supersaiyyans.packet.Packet
import com.supersaiyyans.util.Logger
import com.supersaiyyans.util.Logger._
import src.main.scala.com.supersaiyyans.actors.CommonMessages.SwitchServiceData
import src.main.scala.com.supersaiyyans.util.Commons.AssignedServiceId

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._


/*
TODO: Support multi service discovery
 */
class TheEnchantress(repoActor: ActorRef, discoveryActors: List[ActorRef]) extends FSM[State, Data] with LoggingFSM[State, Data] {


  startWith(Processing, EnchantressData(Map.empty[AssignedServiceId, ActorRef]))

  when(Processing) {
    case Event(service: DiscoveredService, data: EnchantressData) =>
      implicit val askTimeout = Timeout(1 minute)
      debug(s"${self.path.name}: New Service Discovered!!!!")
      debug(s"New Service discovered with profile id: ${service.profileId}")
      sender.ask(WhichProtocol).mapTo[ChannelType].map {
        protocol =>
          ServiceActorDecider(UUID.randomUUID(), ProfileType(service.profileId.toInt)
            , service.deviceId, service.serviceId, repoActor, protocol)(context)

      }
      stay

    case Event(AddServiceActor(assignedServiceId, actor), data: EnchantressData) =>
      stay using EnchantressData(
        data.serviceActors +
          ((assignedServiceId, actor)))

    case Event(Terminated(message), data) =>
      debug(s"RepoActor is down,enchantress wont work properly: ${message}")
      stay

    case msg@_ =>
      debug(s"Enchantress received unknown message!")
      stay
  }

  def ServiceActorDecider(assignedServiceId: AssignedServiceId, profileId: ProfileType, deviceId: String, serviceId: String, repoActor: ActorRef, protocolType: ChannelType)
                         (context: ActorContext) = {

    profileId match {
      case ProfileType.SwitchProfile =>
        self ! AddServiceActor(assignedServiceId,
          context.actorOf(Props(new SwitchServiceActor(
            assignedServiceId
            , deviceId
            , serviceId
            , SwitchServiceData("OFF")
            , repoActor
            , protocolType))))
    }
  }


}


object TheEnchantress {

  sealed trait State

  object Processing extends State

  sealed trait Data

  sealed case class EnchantressData(serviceActors: Map[AssignedServiceId, ActorRef]) extends Data

  case class DiscoveredService(deviceId: String, serviceId: String, profileId: String)

  case class ProcessPacketFromUser(packet: Packet)

  case class ProcessSystemPacket(packet: Packet)

  def props(repoActor: ActorRef, discoveryActors: List[ActorRef]) = Props(new TheEnchantress(repoActor, discoveryActors))

  trait ServiceState

  case class SwitchServiceState(value: String)

  case class AddServiceActor(assignedServiceId: AssignedServiceId, serviceActor: ActorRef)

  case class ServiceData(name: String, val serviceId: String, deviceId: String, state: ServiceState)

}

object MQTTUtils {


}

object ProfileType extends Enumeration(initial = 1) {
  type ProfileType = Value
  val DiscoveryProfile, SwitchProfile = Value
}
