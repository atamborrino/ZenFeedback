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

class OrganiserSender extends WebSocketSender[JsValue] {
  def customReceive: Receive = {
    case Connected(id) =>
      context.parent ! OrganiserConnected(id)
  }
  
  override def receive = customReceive orElse super.receive 
}

class ResultPageSender extends WebSocketSender[JsValue] {
  def customReceive: Receive = {
    case Connected(id) =>
      context.parent ! ResultPageConnected(id)
  }
  
  override def receive = customReceive orElse super.receive 
}

class AttendantSender extends WebSocketSender[JsValue] {
  def customReceive: Receive = {
    case Connected(id) =>
      context.parent ! AttendantConnect(id)
  }
  
  override def receive = customReceive orElse super.receive 
}

case class Connected(id: String)

class CustomSupervisor extends Supervisor {
  var organisers = Map.empty[String, Member]
  var attendants = Map.empty[String, Member]
  var resultPages = Map.empty[String, Member]
  
  def customReceive: Receive = {
    case ResultPageConnected(id) =>
      members.get(id) foreach { member => 
        resultPages += (id -> member)
        member.receiver ! Connected(id)
      }
    
    case AttendantConnect(id) =>
      members.get(id) foreach { member =>
        attendants += (id -> member)
        member.receiver ! Connected(id)
      }
    
    case OrganiserConnected(id) =>
      members.get(id) foreach { member =>
        resultPages += (id -> member)
        member.receiver ! Connected(id)
      }
      
    case SendToOrganisers(from, data) =>
      organisers foreach {
        case (_, member) => member.sender ! Broadcast(from, data)
      }

    case SendToResultPages(from, data) =>
      resultPages foreach {
        case (_, member) => member.sender ! Broadcast(from, data)
      }

    case SendToAttendants(from, data) =>
      attendants foreach {
        case (_, member) => member.sender ! Broadcast(from, data)
      } 

    case Disconnected(id) =>
      members.get(id).foreach { m =>
        members -= id
        m.sender ! Disconnected(id)
        m.receiver ! PoisonPill
        m.sender ! PoisonPill
      }
      organisers -= id
      attendants -= id
      resultPages -= id
      
  }

  override def receive = customReceive orElse super.receive
}
