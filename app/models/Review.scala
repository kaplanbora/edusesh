package models

import java.time.LocalDateTime

import forms.DateFormats
import play.api.libs.json.Json

case class Review(
    id: Long,
    sessionId: Long,
    traineeId: Long,
    rating: Double,
    title: String,
    comment: Option[String],
    date: LocalDateTime
)

object Review extends DateFormats {
  implicit val reviewFormat = Json.format[Review]
}
