package models
import java.util.UUID

case class Question(name: String, uuid: UUID = UUID.randomUUID())
