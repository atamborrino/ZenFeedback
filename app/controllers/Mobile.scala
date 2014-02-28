package controllers

import play.api._
import play.api.mvc._

object Mobile extends Controller {

  def takePartIn(name: String) = Action {
    Ok(views.html.mobile.takePartIn(name))
  }

  def stream(name: String) = Action {
    Ok
  }
}
