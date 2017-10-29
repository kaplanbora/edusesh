package auth

import io.igl.jwt._
import models.Account
import util.Logger
import java.time.ZonedDateTime
import play.api.libs.json._

case class UserRole(value: String) extends ClaimValue {
  override val field: ClaimField = UserRole
  override val jsValue: JsValue = JsString(value)
}

object UserRole extends (String => UserRole) with ClaimField {
  override def attemptApply(value: JsValue): Option[ClaimValue] = value.asOpt[String].map(apply)

  override val name: String = "role"
}

object Token extends Logger {
  def generate(account: Account): String = {
    new DecodedJwt(
      Seq(Alg(Algorithm.HS256), Typ("JWT")),
      Seq(Sub(s"${account.id}"), UserRole(account.userType), Exp(ZonedDateTime.now().plusWeeks(2).toEpochSecond))
    ).encodedAndSigned(Security.tokenKey)
  }
}
