package com.supersaiyyans.controller

import com.supersaiyyans.actors.ServicesRepoActor.{ServiceData, FetchAll}
import com.supersaiyyans.actors.{ServicesRepoActor, TheEnchantressSupervisor, MQTTDiscoveryActor}
import com.supersaiyyans.packet.JsonCmdPacket
import play.api.libs.concurrent.Akka
import play.api.mvc.{Action, Controller}
import akka.pattern.ask
import com.supersaiyyans.util.Logger._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._


import scala.concurrent.Future

object MainController extends Controller {

  import play.api.Play.current

  val repoActor = Akka.system.actorOf(ServicesRepoActor.props, "RepoActor")

  val enchantress = Akka.system.actorOf(TheEnchantressSupervisor.props(repoActor))

  def serviceCommand = Action(parse.json) {
    request =>
      request.body.validate[JsonCmdPacket].map {
        jsonRequest =>
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


  def listServices = Action.async {
    request =>
      implicit val timeout = akka.util.Timeout(5 minutes)
      val listOfServices: Future[List[ServiceData]] = repoActor.ask(FetchAll).mapTo[List[ServiceData]]
      listOfServices.map{
        services =>
          services.foreach{x=>debug(x.toString)}
          Ok("Services found!!!")
      }

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
