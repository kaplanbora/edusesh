package forms

import java.time.LocalDateTime

import play.api.libs.json.Json

case class SessionForm(
    name: String,
    traineeId: Long,
    instructorId: Long,
    topicId: Long,
    date: LocalDateTime,
    isApproved: Boolean,
    isCompleted: Boolean
)

case class ReportForm(
    sessionId: Long,
    userId: Long,
    title: String,
    description: String,
    isResolved: Boolean,
    date: LocalDateTime
)

case class ReviewFrom(
    sessionId: Long,
    traineeId: Long,
    rating: Double,
    title: String,
    comment: Option[String],
    date: LocalDateTime
)

case class SessionFileForm(
    sessionId: Long,
    name: String,
    link: String
)

object SessionForms extends DateFormats {
  implicit val sessionFormFormat = Json.format[SessionForm]
  implicit val reportFormFormat = Json.format[ReportForm]
  implicit val reviewFormFormat = Json.format[ReviewFrom]
  implicit val sessionFileFormFormat = Json.format[SessionFileForm]
}
