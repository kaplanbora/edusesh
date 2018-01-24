package models

import java.time.LocalDateTime

import forms.DateFormats
import play.api.libs.json.Json

case class LiveSession(
    id: Long,
    name: String,
    traineeId: Long,
    instructorId: Long,
    topicId: Long,
    date: LocalDateTime,
    isApproved: Boolean,
    isCompleted: Boolean
)

object LiveSession extends DateFormats {
  implicit val sessionFormat = Json.format[LiveSession]
}
