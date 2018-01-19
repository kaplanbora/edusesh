package models

import play.api.libs.json.Json

case class InstructorProfile(
    id: Long,
    firstName: Option[String],
    lastName: Option[String],
    description: Option[String],
    occupation: Option[String],
    imageLink: Option[String],
    videoLink: Option[String],
    hourlyRate: Double,
    userId: Long
)

object InstructorProfile {
  implicit val instructorProfileFormat = Json.format[InstructorProfile]
}
