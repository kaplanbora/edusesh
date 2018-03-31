package models

import java.time.LocalDateTime

import play.api.libs.json.Json
import util.DateFormats

case class LiveSession(
    id: Long,
    name: String,
    description: String,
    traineeId: Long,
    instructorId: Long,
    topicId: Long,
    date: LocalDateTime,
    isApproved: Boolean,
    isCompleted: Boolean,
    isDeleted: Boolean
)

object LiveSession extends DateFormats {
  implicit val sessionFormat = Json.format[LiveSession]
}
