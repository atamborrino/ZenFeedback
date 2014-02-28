package controllers

import akka.actor._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import models._

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Concurrent
import scala.concurrent.Future
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import JsonFormats._

import play.api.data._
import play.api.data.Forms._
import java.util.UUID
import akka.pattern.{ ask, pipe }

import org.mandubian.actorroom._
import play.api.libs.concurrent.Akka
import akka.util.Timeout
import scala.concurrent.duration._

case class SendToOrganisers[A](from: String, payload: A)
case class SendToResultPages[A](from: String, payload: A)
case class SendToAttendants[A](from: String, payload: A)

case class SendNewQuestion(q: Question, answsers: Seq[Answer])
case class Vote(questionId: UUID, answerId: UUID)

class Organiser extends Actor {
  def receive = {
    case Connected(id) =>

    case Received(id, js: JsValue) =>
      val question = Question((js \ "question" \ "name").as[String])

      val answers = (js \ "answers").as(
        Reads.seq(
            (__ \ "name").read[String]
        )
      ) map { str => Answer(str) }

      context.parent ! SendNewQuestion(question, answers)

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
      val questionId = UUID.fromString((js \ "questionId").as[String])
      val answerId = UUID.fromString((js \ "answerId").as[String])
      context.parent ! Vote(questionId, answerId)

  }
}

case object RoomNameAlreadyExistant
case class RoomCreated(room: Room, name: String)
case class CreateRoom(name: String)
case class GetRoom(name: String)

class Rooms extends Actor {
  var rooms = Map("test" -> Room(Props[CustomSupervisor]))

  def receive = {
    case CreateRoom(name) =>
      val maybeRoom = rooms.get(name)

      maybeRoom match {
        case Some(_) =>
          sender ! RoomNameAlreadyExistant
        case None =>
          val newRoom = Room(Props[CustomSupervisor])
          rooms = rooms + (name -> newRoom)
          sender ! RoomCreated(newRoom, name)
      }

    case GetRoom(name: String) =>
      rooms.get(name) match {
        case Some(room) =>
          sender ! Option(room)
          println("sending room")
        case None =>
          sender ! Option.empty[Room]
          println("no room")
      }
  }
}


object Application extends Controller {
  implicit val timeout = Timeout(5 seconds)

  val rooms = Akka.system.actorOf(Props[Rooms])

  def index = Action {
    Ok(views.html.index())
  }

  def results(name: String) = Action { req =>
    val host = req.host.split(":80").headOption getOrElse req.host
    Ok(views.html.results(name, host))
  }

  def resultsJs(name: String) = Action { implicit request =>
    Ok(views.js.results(name))
  }

  case class RoomData(name: String)
  val roomForm = Form(
    mapping(
      "name" -> text
    )(RoomData.apply)(RoomData.unapply)
  )

  def createRoom() = Action.async { implicit req =>

    val userData = roomForm.bindFromRequest.get
    val name = userData.name
      (rooms ? CreateRoom(name)) map {
        case RoomNameAlreadyExistant =>
          Ok(s"Room with name $name already exists")

        case RoomCreated(newRoom, secretId) =>
          Redirect(routes.Application.getRoomOrga(name))
      }

  }

  def getRoomOrga(name: String) = Action { req =>
    val host = req.host.split(":80").headOption getOrElse req.host
    Ok(views.html.orga(name, host))
  }

  def connectOrgaWs(name: String) = Room.async {
    val orgaid = UUID.randomUUID().toString
    println("connection...")

    val futureRoom = (rooms ? GetRoom(name)).mapTo[Option[Room]] map (maybeRoom => maybeRoom.get)

    futureRoom map (room => room.websocket[JsValue]((_: RequestHeader) => orgaid, Props[Organiser], Props[OrganiserSender]))
  }

  def connectAttendantWS(name: String) = Room.async {
    val userid = UUID.randomUUID().toString

    val futureRoom = (rooms ? GetRoom(name)).mapTo[Option[Room]] map (maybeRoom => maybeRoom.get)

    futureRoom map (room => room.websocket[JsValue]((_: RequestHeader) => userid, Props[Attendant], Props[AttendantSender]))
  }

  def connectResultsWS(name: String) = Room.async {
    val id = UUID.randomUUID().toString

    val futureRoom = (rooms ? GetRoom(name)).mapTo[Option[Room]] map (maybeRoom => maybeRoom.get)

    futureRoom map (room => room.websocket[JsValue]((_: RequestHeader) => id, Props[ResultPage], Props[ResultPageSender]))
  }
}
