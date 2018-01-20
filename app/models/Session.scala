package models

import java.time.LocalDateTime

import play.api.libs.json.Json

case class Session(
    id: Long,
    name: String,
    traineeId: Long,
    instructorId: Long,
    topicId: Long,
    date: LocalDateTime,
    isApproved: Boolean,
    isCompleted: Boolean
)

object Session {
  import forms.TimestampFormats._

  implicit val sessionFormat = Json.format[Session]
}
