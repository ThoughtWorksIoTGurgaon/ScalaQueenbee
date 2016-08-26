package com.supersaiyyans.actors

import akka.actor.{Props, FSM}
import ServicesRepoActor._

class ServicesRepoActor extends FSM[RepoState,Data]{

  startWith(Running,ServicesData(Map.empty))

  when(Running){
    case Event(command: UpdateState,currentState: ServicesData) =>
      val newData =
        currentState
          .servicesData
          .updated(command.serviceData.serviceId,command.serviceData)
    stay using ServicesData(newData)

    case Event(FetchAll,currentState: ServicesData) =>
      sender ! currentState.servicesData.values.toList
      stay

    case Event(fetchData: FetchServiceData,currentState: ServicesData) =>
      sender ! currentState.servicesData.get(fetchData.serviceId)
      stay
  }

}

object ServicesRepoActor {

  def props = Props[ServicesRepoActor]

  sealed trait RepoState
  object Initializing extends RepoState
  object Running extends RepoState

  sealed trait Data
  sealed case class ServicesData(servicesData: Map[String,ServiceData]) extends Data

  trait ServiceState

  case class SwitchServiceState(value: String)

  case class ServiceData(name: String,val serviceId: String,deviceId: String,state: ServiceState)

  sealed trait SupportedEventsByRepo
  case class UpdateState(serviceData: ServiceData) extends SupportedEventsByRepo
  object FetchAll extends SupportedEventsByRepo
  case class FetchServiceData(serviceId: String) extends SupportedEventsByRepo



}
