package models

import java.time.LocalDateTime

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

object Review {
  import forms.TimestampFormats._

  implicit val reviewFormat = Json.format[Review]
}
