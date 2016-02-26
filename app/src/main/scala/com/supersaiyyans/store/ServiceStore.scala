package src.main.scala.com.supersaiyyans.store

import src.main.scala.com.supersaiyyans.service.SwitchService.SwitchServiceState
import src.main.scala.com.supersaiyyans.service.{Service, SwitchService}


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
    val serviceAddr: String = deviceId + ":" + serviceId
    if (!serviceMap.contains(serviceAddr)){
      println("Adding switch service")
      serviceMap += serviceAddr -> new SwitchService(serviceAddr)
    }
  }

}
