package controllers

import akka.actor._

import play.api._
import play.api.mvc._
import play.api.libs.json._

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

}