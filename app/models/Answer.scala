package models
import java.util.UUID

case class Answer(name: String, uuid: UUID = UUID.randomUUID())
