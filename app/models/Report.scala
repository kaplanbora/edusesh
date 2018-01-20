package models

import java.time.LocalDateTime

import play.api.libs.json.Json

case class Report(
    id: Long,
    sessionId: Long,
    userId: Long,
    title: String,
    description: String,
    isResolved: Boolean,
    date: LocalDateTime
)

object Report {
  import forms.TimestampFormats._

  implicit val reportFormat = Json.format[Report]
}
