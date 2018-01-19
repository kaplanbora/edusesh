package forms

import models._
import play.api.libs.json.Json

case class UserRegisterForm(email: String, password: String, userRole: UserRole)
case class UserCredentialsForm(email: String, password: String)
case class TraineeProfileForm(firstName: Option[String], lastName: Option[String], imageLink: Option[String])
case class InstructorProfileForm(
    firstName: Option[String],
    lastName: Option[String],
    description: Option[String],
    occupation: Option[String],
    imageLink: Option[String],
    videoLink: Option[String],
    hourlyRate: Double
)

object UserForms {
  import UserCredentials.userRoleFormat

  implicit val registerFormFormat = Json.format[UserRegisterForm]
  implicit val credentialsFormFormat = Json.format[UserCredentialsForm]
  implicit val traineeProfileFormFormat = Json.format[TraineeProfileForm]
  implicit val instructorProfileFormFormat = Json.format[InstructorProfileForm]
}
