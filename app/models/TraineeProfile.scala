package models

import play.api.libs.json.Json

case class TraineeProfile(
    id: Long,
    firstName: Option[String],
    lastName: Option[String],
    imageLink: Option[String],
    userId: Long
)

object TraineeProfile {
  implicit val traineeProfileFormat = Json.format[TraineeProfile]
}
