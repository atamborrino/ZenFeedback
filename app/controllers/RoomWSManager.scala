package controllers

import akka.actor._

import play.api._
import play.api.mvc._
import play.api.libs.json._

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import org.mandubian.actorroom._
import java.util.UUID

import models._

case class OrganiserConnected(id: String)
case class ResultPageConnected(id: String)
case class AttendantConnect(id: String)

class OrganiserSender extends WebSocketSender[JsValue] {
  def customReceive: Receive = {
    case org.mandubian.actorroom.Connected(id) =>
      println("Connected")
      context.parent ! OrganiserConnected(id)
  }

  override def receive = customReceive orElse super.receive
}

class ResultPageSender extends WebSocketSender[JsValue] {
  def customReceive: Receive = {
    case org.mandubian.actorroom.Connected(id) =>
      context.parent ! ResultPageConnected(id)
  }

  override def receive = customReceive orElse super.receive
}

class AttendantSender extends WebSocketSender[JsValue] {
  def customReceive: Receive = {
    case org.mandubian.actorroom.Connected(id) =>
      println(id)
      context.parent ! AttendantConnect(id)
  }

  override def receive = customReceive orElse super.receive
}

case class Connected(id: String)

class CustomSupervisor extends Supervisor {
  var organisers = Map.empty[String, Member]
  var attendants = Map.empty[String, Member]
  var resultPages = Map.empty[String, Member]
  
  var current = Option.empty[(Question, Seq[Answer])]
  var votes = Map.empty[UUID, Int].withDefaultValue(0)

  def customReceive: Receive = {
    case ResultPageConnected(id) =>
      members.get(id) foreach { member =>
        resultPages += (id -> member)
        member.receiver ! Connected(id)
      }

    case AttendantConnect(id) =>
      members.get(id) foreach { member =>
        attendants = attendants + (id -> member)
        member.receiver ! Connected(id)
      }
      println(attendants)

    case OrganiserConnected(id) =>
      members.get(id) foreach { member =>
        resultPages += (id -> member)
        member.receiver ! Connected(id)
      }
      
    case SendNewQuestion(q: Question, answsers: Seq[Answer]) =>
      current = Some((q, answsers))
      self ! SendToAttendants("", Json.obj())
      self ! SendToResultPages("", Json.obj())
      
    case Vote(answer) =>
      votes = votes + (answer.uuid -> (votes(answer.uuid) + 1))
      self ! SendToResultPages("", Json.obj())

    case SendToOrganisers(from, data) =>
      organisers foreach {
        case (_, member) => member.sender ! Broadcast(from, data)
      }

    case SendToResultPages(from, data) =>
      resultPages foreach {
        case (_, member) => member.sender ! Broadcast(from, data)
      }

    case SendToAttendants(from, data) =>
      println(data)
      println(attendants)
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
