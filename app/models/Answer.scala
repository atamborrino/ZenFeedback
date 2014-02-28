package models
import java.util.UUID
import play.api.libs.json._

case class Answer(name: String, var nbVotes: Int = 0, uuid: UUID = UUID.randomUUID())
