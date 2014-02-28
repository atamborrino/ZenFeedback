package controllers

import akka.actor._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import models._

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
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
case class Vote(answer: Answer)

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

case object RoomNameAlreadyExistant
case class RoomCreated(room: Room, name: String)
case class CreateRoom(name: String)
case class GetRoom(name: String)

class Rooms extends Actor {
  var rooms = Map.empty[String, Room]
  
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
        case Some(room) => sender ! Option(room)
        case None => sender ! Option.empty[Room]
      }
  }
}


object Application extends Controller {
  implicit val timeout = Timeout(5 seconds)
  
  val rooms = Akka.system.actorOf(Props[Rooms])

  def index = Action {
    Ok(views.html.index())
  }

  def results(name: String) = Action {
    val question = Question("Favorite phone OS?")
    val answers = Seq("iPhone", "Android", "Windows").map(Answer(_))
    Ok(views.html.results(name, question, answers))
  }

  def resultsWS(name: String) = WebSocket.async[JsValue] { request =>
    ???
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

  def getRoomOrga(name: String) = Action {
    Ok(views.html.orga(name))
  }
  
  def connectOrgaWs(name: String) = Room.async {
    val orgaid = UUID.randomUUID().toString
    
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
