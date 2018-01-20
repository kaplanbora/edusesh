package auth

import io.igl.jwt._
import models.UserCredentials
import util.Logger
import java.time.ZonedDateTime

import play.api.libs.json._

case class Role(value: String) extends ClaimValue {
  override val field: ClaimField = Role
  override val jsValue: JsValue = JsString(value)
}

object Role extends (String => Role) with ClaimField {
  override def attemptApply(value: JsValue): Option[ClaimValue] = value.asOpt[String].map(apply)
  override val name: String = "role"
}

object Token extends Logger {
  def generate(user: UserCredentials): String =
    new DecodedJwt(
      Seq(Alg(Algorithm.HS256), Typ("JWT")),
      Seq(Sub(s"${user.id}"), Role(user.userRole.role), Exp(ZonedDateTime.now().plusWeeks(2).toEpochSecond))
    ).encodedAndSigned(Security.tokenKey)


  def validate(token: String): Option[Jwt] =
    DecodedJwt.validateEncodedJwt(
      token,
      Security.tokenKey,
      Algorithm.HS256,
      Set(Typ),
      Set(Sub, Role, Exp)
    ).toOption
}
