package com.supersaiyyans.service

import com.supersaiyyans.store.ServiceStore


object DiscoveryService {

  type ProfileId = String
  type ServiceId = String
  type ServiceInfo = (ProfileId,ServiceId)

  def process(deviceId: String, byteVector: Vector[Byte]) = {
    val serviceCount: Byte = byteVector(9)
    val services = customZip2(byteVector.toList.drop(10))
    services.foreach{
      service=>
        ServiceStore.add(deviceId,service._2,service._1)
    }
  }

  def customZip2(byteVector: List[Byte]): List[ServiceInfo]= {
    def customZip2(byteVector: List[Byte],acc: List[ServiceInfo]): List[ServiceInfo] = {
      byteVector match {
        case Nil => acc
        case x::Nil => acc
        case x::y::xs => customZip2(xs,acc :+ (x.toInt.toString,y.toInt.toString))

      }
    }
    customZip2(byteVector,Nil)
  }

}

