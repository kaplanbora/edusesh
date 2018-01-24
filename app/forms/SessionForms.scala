package forms

import java.time.LocalDateTime

import play.api.libs.json.Json
import util.DateFormats

case class SessionForm(
    name: String,
    traineeId: Long,
    instructorId: Long,
    topicId: Long,
    date: LocalDateTime,
    isApproved: Boolean,
    isCompleted: Boolean
)

case class SessionUpdateForm(isApproved: Boolean, isCompleted: Boolean)

case class ReportForm(
    sessionId: Long,
    userId: Long,
    title: String,
    description: String,
    isResolved: Boolean,
)

case class ReviewForm(
    sessionId: Long,
    traineeId: Long,
    rating: Double,
    title: String,
    comment: Option[String],
)

case class ReviewUpdateForm(rating: Double, title: String, comment: Option[String])

case class SessionFileForm(
    sessionId: Long,
    name: String,
    link: String
)

object SessionForms extends DateFormats {
  implicit val sessionFormFormat = Json.format[SessionForm]
  implicit val reportFormFormat = Json.format[ReportForm]
  implicit val reviewFormFormat = Json.format[ReviewForm]
  implicit val sessionFileFormFormat = Json.format[SessionFileForm]
}
