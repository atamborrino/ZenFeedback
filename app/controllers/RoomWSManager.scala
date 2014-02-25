package controllers

import akka.actor._

import play.api._
import play.api.mvc._
import play.api.libs.json._

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import org.mandubian.actorroom._

case class OrganiserConnected(id: String)
case class ResultPageConnected(id: String)
case class AttendantConnect(id: String)

class OrganiserSender[Payload](implicit msgFormatter: AdminMsgFormatter[Payload]) extends WebSocketSender[Payload] {
  def customReceive: Receive = {
    case Connected(id) =>
      context.parent ! OrganiserConnected(id)
  }
  
  override def receive = customReceive orElse super.receive 
}

class ResultPageSender[Payload](implicit msgFormatter: AdminMsgFormatter[Payload]) extends WebSocketSender[Payload] {
  def customReceive: Receive = {
    case Connected(id) =>
      context.parent ! ResultPageConnected(id)
  }
  
  override def receive = customReceive orElse super.receive 
}

class AttendantSender[Payload](implicit msgFormatter: AdminMsgFormatter[Payload]) extends WebSocketSender[Payload] {
  def customReceive: Receive = {
    case Connected(id) =>
      context.parent ! AttendantConnect(id)
  }
  
  override def receive = customReceive orElse super.receive 
}

class CustomSupervisor extends Supervisor {
  var organisers = Map.empty[String, Member]
  
  def customReceive: Receive = {
    case ResultPageConnected(id) =>
      organisers.get(id) foreach (member => organisers += (id -> member))
      
      
  }

  override def receive = customReceive orElse super.receive
}



case class SendToOrganisers[A](payload: A)
case class SendToResultPages[A](payload: A)
case class BroadcastToAttendants[A](payload: A)

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