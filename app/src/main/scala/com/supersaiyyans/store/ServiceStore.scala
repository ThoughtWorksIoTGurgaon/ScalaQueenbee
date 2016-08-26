package com.supersaiyyans.store

import akka.actor.Actor
import com.supersaiyyans.service.{ServiceState, SwitchService, Service}
import com.supersaiyyans.service.SwitchService.SwitchServiceState


class TheEnchantress extends Actor {

  def receive = {
    case _=>
  }

}


object ServiceStore {
  type DeviceId = String
  var serviceMap = Map[DeviceId, Service[ServiceState]]()

  def get(serviceAddr: String): Option[Service[ServiceState]] = {
    serviceMap.get(serviceAddr)
  }

  def listAll() = {
    serviceMap
  }



  def add(deviceId: String, serviceId: String,profileId: String) {
    val serviceAddr: String = {
      deviceId + ":" + serviceId
    }
    if (!serviceMap.contains(serviceAddr)){
      println("Adding switch service")
//      serviceMap += serviceAddr -> new SwitchService(serviceAddr,SwitchServiceState("OFF"))

    }
  }

}
