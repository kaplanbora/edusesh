package models

import java.time.LocalDateTime
import play.api.libs.json._

sealed abstract class UserRole(val role: String)

case object AdminRole extends UserRole("admin")

case object InstructorRole extends UserRole("instructor")

case object TraineeRole extends UserRole("instructor")

case class UserCredentials(
    id: Long,
    email: String,
    password: String,
    creationDate: LocalDateTime,
    userRole: UserRole
)

object UserCredentials {

  import forms.TimestampFormats._

  def userRoleToString(userRole: UserRole): String = userRole.role

  def stringToUserRole(role: String): UserRole = role match {
    case "admin" => AdminRole
    case "instructor" => InstructorRole
    case "trainee" => TraineeRole
  }

  implicit object userRoleFormat extends Format[UserRole] {
    def reads(json: JsValue) = (json \ "role").get.as[String] match {
      case "admin" => JsSuccess(AdminRole)
      case "instructor" => JsSuccess(InstructorRole)
      case "trainee" => JsSuccess(TraineeRole)
      case _ => JsError("Unknown user role.")
    }

    def writes(userRole: UserRole) = JsString(userRole.role)
  }

  implicit val userCredentialsFormat = Json.format[UserCredentials]
}