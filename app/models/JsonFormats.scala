package models
import play.api.libs.json.Json
import play.api.libs.json._
import java.util.UUID

object JsonFormats {
  implicit val uuidWrites = new Writes[UUID] {
    def writes(uuid: UUID): JsValue = Json.toJson(uuid.toString)
  }

  implicit val uuidReads = new Reads[UUID] {
    def reads(json: JsValue): JsResult[UUID] = JsSuccess(UUID.fromString(json.toString))
  }

  implicit val answerFormat = Json.format[Answer]
  
  implicit val questionFormat = Json.format[Question]
}
