package com.supersaiyyans.controller

import akka.actor.Props
import com.supersaiyyans.packet.JsonCmdPacket
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import src.main.scala.com.supersaiyyans.actors.{MQTTPublisherProxySupervisor, MQTTSubscriberProxySupervisor}
import src.main.scala.com.supersaiyyans.store.ServiceStore

object MainController extends Controller{

  val mqttPublisherProxySupervisor = Akka.system.actorOf(Props[MQTTPublisherProxySupervisor],"MQTTPublisherProxySupervisor")
 val mqttSubscriberProxySupervisor = Akka.system.actorOf(Props[MQTTSubscriberProxySupervisor],"MQTTSubscriberProxySupervisor")


  def serviceCommand= Action(parse.json){
    request=>
      request.body.validate[JsonCmdPacket].map {
        packet=>
          mqttPublisherProxySupervisor ! packet
          Ok(packet.toString).withHeaders("Access-Control-Allow-Origin"->"*")
      }.recoverTotal(e=>BadRequest)


  }

  def option(path: String) = Action{
      Ok("").withHeaders(
        "Access-Control-Allow-Origin" -> "*",
        "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
        "Access-Control-Allow-Headers" -> "Accept, Origin, Content-type, X-Json, X-Prototype-Version, X-Requested-With",
        "Access-Control-Allow-Credentials" -> "true",
        "Access-Control-Max-Age" -> (0).toString)
  }
  def listServices = Action{
    request=>
      Ok(
        Json.toJson(
        ServiceStore
          .listAll()
          .map{
            deviceWithService=>
              deviceWithService._2.toJson()
          }.toArray))
        .withHeaders("Content-Type" -> "application/json"
        ,"Access-Control-Allow-Origin"->"*")
  }
}
