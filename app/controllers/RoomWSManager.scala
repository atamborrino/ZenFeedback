package controllers

import akka.actor._

import play.api._
import play.api.mvc._
import play.api.libs.json._


import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import org.mandubian.actorroom._

case class SendToOrganiser[A](payload: A)
case class SendToResultPage[A](payload: A)
case class BroadcastToAttendants[A](payload: A)

class OrganiserSender extends WebSocketSender {
  def customReceive = {
    
  }
  
  override def receive = customReceive orElse super.receive 
}



class Organiser extends Actor {
  def receive = {
    case Received(from, js: JsValue) =>
      // ...
  }
}

class ResultPage extends Actor {
  def receive = {
    case Received(from, js: JsValue) =>
      // ...
  }
}

class Attendant extends Actor {
  def receive = {
    case Received(from, js: JsValue) =>
      // ...
  }
}