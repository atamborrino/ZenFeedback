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

  val question = Question("Favorite phone OS?")
  val answers = Seq("iPhone", "Android", "Windows").map(Answer(_))

  def results(name: String) = Action {
    Ok(views.html.results(name, question, answers))
  }

  def resultsWS(name: String) = WebSocket.async[JsValue] { request =>
    Future(TmpWsForTest.ws(answers))
  }

  def resultsJs(name: String) = Action { implicit request =>
    Ok(views.js.results(name))
  }


  val room = Room(Props[CustomSupervisor])

  def takePartIn(name: String) = Action {
    Ok(views.html.takePartIn(name))
  }

  def connect(id: String) = {
    //room.websocket[JsValue](id, Props[Organiser], Props[OrganiserSender])
  }

}

object TmpWsForTest {
  import play.api.libs.concurrent.Akka
  import play.api.Play.current
  import scala.concurrent.duration._
  import play.api.libs.iteratee.Concurrent.Channel
  import scala.util.Random


  def ws(answers: Seq[Answer]): (Iteratee[JsValue, _], Enumerator[JsValue]) = {
    val (enumerator, channel) = Concurrent.broadcast[JsValue]

    launchScheduler(channel, answers)
    val iteratee = Iteratee.foreach[JsValue] { _ => }
    (iteratee, enumerator)
  }

  private def launchScheduler(channel: Channel[JsValue], answers: Seq[Answer]) = {
    Akka.system.scheduler.schedule(0.milliseconds, 3.seconds) {
      for (answer <- Random.shuffle(answers).headOption) {
        answer.nbVotes += 1
        channel.push(Json.toJson(answer))
      }
    }
  }
}
