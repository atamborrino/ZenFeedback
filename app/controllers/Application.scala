package controllers

import akka.actor._

import play.api._
import play.api.mvc._
import play.api.libs.json._

import models._

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import org.mandubian.actorroom._

case class SendToOrganisers[A](from: String, payload: A)
case class SendToResultPages[A](from: String, payload: A)
case class SendToAttendants[A](from: String, payload: A)

class Organiser extends Actor {
  def receive = {
    case Connected(id) =>
      //...

    case Received(id, js: JsValue) =>
      // ...
  }
}

class ResultPage extends Actor {
  def receive = {
    case Connected(id) =>
      //...

    case Received(id, js: JsValue) =>
      // ...
  }
}

class Attendant extends Actor {
  def receive = {
    case Connected(id) =>
      //...

    case Received(id, js: JsValue) =>
      // ...
  }
}

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def results(name: String) = Action {
    val question = Question("Favorite phone OS?")
    val answers = Seq("iPhone", "Android", "Windows").map(Answer(_))
    Ok(views.html.results(name, question, answers))
  }

  def resultsWS(name: String) = WebSocket.async[JsValue] { request =>
    ???
  }

  val room = Room(Props[CustomSupervisor])

  def takePartIn(name: String) = Action {
    Ok(views.html.takePartIn(name))
  }

  def connect(id: String) = {
    //room.websocket[JsValue](id, Props[Organiser], Props[OrganiserSender])
  }

}
