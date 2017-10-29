package forms

import play.api.libs.json.Json

case class AccountForm(email: String, password: String, firstName: String, lastName: String)

case class LoginForm(email: String, password: String)

object AccountForms {
  implicit val accountFormFormat = Json.format[AccountForm]
  implicit val loginFormFormat = Json.format[LoginForm]
}
