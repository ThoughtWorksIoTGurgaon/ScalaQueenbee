package com.supersaiyyans.store

import com.supersaiyyans.service.{SwitchService, Service}
import com.supersaiyyans.service.SwitchService.SwitchServiceState


object ServiceStore {
  type DeviceId = String
  var serviceMap = Map[DeviceId, Service[SwitchServiceState]]()

  def get(serviceAddr: String): Option[Service[SwitchServiceState]] = {
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
      serviceMap += serviceAddr -> SwitchService(serviceAddr,SwitchServiceState("OFF"))

    }
  }

}
