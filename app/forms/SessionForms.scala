package forms

import java.time.LocalDateTime

import play.api.libs.json.Json
import util.DateFormats

case class SessionForm(
    name: String,
    description: String,
    instructorId: Long,
    topicId: Long,
    date: LocalDateTime,
)

case class SessionUpdateForm(isApproved: Boolean, isCompleted: Boolean)

case class ReportForm(
    title: String,
    description: String,
)

case class ReviewForm(
    traineeId: Long,
    rating: Double,
    title: String,
    comment: Option[String],
)

case class ReviewUpdateForm(rating: Double, title: String, comment: Option[String])

case class SessionFileForm(
    name: String,
    link: String
)

object SessionForms extends DateFormats {
  implicit val sessionFormFormat = Json.format[SessionForm]
  implicit val sessionUpdateFormFormat = Json.format[SessionUpdateForm]
  implicit val reportFormFormat = Json.format[ReportForm]
  implicit val reviewFormFormat = Json.format[ReviewForm]
  implicit val reviewUpdateFormFormat = Json.format[ReviewUpdateForm]
  implicit val sessionFileFormFormat = Json.format[SessionFileForm]
}
