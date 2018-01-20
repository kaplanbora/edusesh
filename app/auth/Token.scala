package auth

import io.igl.jwt._
import models.Account
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
  def generate(account: Account): String =
    new DecodedJwt(
      Seq(Alg(Algorithm.HS256), Typ("JWT")),
      Seq(Sub(s"${account.id}"), Role(account.userType), Exp(ZonedDateTime.now().plusWeeks(2).toEpochSecond))
    ).encodedAndSigned(Security.tokenKey)


  def validate(token: String): Option[Jwt] =
    DecodedJwt.validateEncodedJwt(
      token,                     // An encoded jwt as a string
      Security.tokenKey,         // The key to validate the signature against
      Algorithm.HS256,           // The algorithm we require
      Set(Typ),                  // The set of headers we require (excluding alg)
      Set(Sub, Role, Exp)   // The set of claims we require
    ).toOption
}
