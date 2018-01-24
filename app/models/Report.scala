package models

import java.time.LocalDateTime

import play.api.libs.json.Json
import util.DateFormats

case class Report(
    id: Long,
    sessionId: Long,
    userId: Long,
    title: String,
    description: String,
    isResolved: Boolean,
    date: LocalDateTime
)

object Report extends DateFormats {
  implicit val reportFormat = Json.format[Report]
}
