package com.supersaiyyans.actors

import akka.actor.{ActorLogging, FSM, Props}
import ServicesRepoActor._

class ServicesRepoActor extends FSM[State, Data] with ActorLogging {

  startWith(Running, ServicesData(Map.empty))

  // TODO - add exception for unexpected messages

  when(Running) {
    case Event(command: UpdateServiceData, oldData: ServicesData) =>
      val newData =
        oldData
          .data
          .updated(command.serviceData.serviceId, command.serviceData)
      stay using ServicesData(newData)

    case Event(FetchAll, currentState: ServicesData) =>
      sender ! currentState.data.values.toList
      stay

    case Event(fetchData: FetchServiceData, currentState: ServicesData) =>
      sender ! currentState.data.get(fetchData.serviceId)
      stay

    case Event(newService: AddService, oldData: ServicesData) =>
      val newData = oldData.data.updated(newService.serviceData.serviceId, newService.serviceData)
      stay using ServicesData(newData)

    case _ => log.error("Unexpected message found")
      sender ! UnexpectedMessage
      stay
  }

}

object ServicesRepoActor {

  def props = Props[ServicesRepoActor]

  sealed trait State

  object Initializing extends State

  object Running extends State

  sealed trait Data

  sealed case class ServicesData(data: Map[String, ServiceData]) extends Data

  trait ServiceState

  case class SwitchServiceState(value: String) extends ServiceState

  // TODO : Enchantress and this should probably use the same thing
  case class ServiceData(name: String, serviceId: String, deviceId: String, state: ServiceState)

  sealed trait SupportedEvent

  object FetchAll extends SupportedEvent

  case class AddService(serviceData: ServiceData) extends SupportedEvent

  case class UpdateServiceData(serviceData: ServiceData) extends SupportedEvent

  case class FetchServiceData(serviceId: String) extends SupportedEvent

  case object UnexpectedMessage  // Todo : Make it more specific


}
