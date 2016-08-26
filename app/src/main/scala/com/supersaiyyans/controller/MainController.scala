package com.supersaiyyans.controller

import akka.actor.Props
import com.supersaiyyans.packet.JsonCmdPacket
import com.supersaiyyans.store.ServiceStore
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

object MainController extends Controller {

  import play.api.Play.current

  //  val mqttPublisherProxySupervisor = Akka.system.actorOf(MQTTPublisherProxySupervisor.props,"MQTTPublisherProxySupervisor")
  //  val mqttSubscriberProxySupervisor = Akka.system.actorOf(MQTTSubscriberProxySupervisor.props,"MQTTSubscriberProxySupervisor")


  def serviceCommand = Action(parse.json) {
    request =>
      request.body.validate[JsonCmdPacket].map {
        jsonRequest =>
          //          mqttPublisherProxySupervisor ! jsonRequest
          Ok(jsonRequest.toString).withHeaders("Access-Control-Allow-Origin" -> "*")
      }.recoverTotal(e => BadRequest)


  }

  def option(path: String) = Action {
    Ok("").withHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Accept, Origin, Content-type, X-Json, X-Prototype-Version, X-Requested-With",
      "Access-Control-Allow-Credentials" -> "true",
      "Access-Control-Max-Age" -> (0).toString)
  }


  def listServices = Action {
    request =>
      Ok("KTHXBYE")
    //      Ok(
    //        Json.toJson(
    //        ServiceStore
    //          .listAll()
    //          .map{
    //            deviceWithService=>
    //              deviceWithService._2.toJson()
    //          }.toArray))
    //        .withHeaders("Content-Type" -> "application/json"
    //        ,"Access-Control-Allow-Origin"->"*")
      }
  }
