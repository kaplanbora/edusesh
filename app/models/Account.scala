package models

import java.sql.Timestamp
import java.time.LocalDateTime

import play.api.libs.json._
import play.api.libs.json.Json._

case class Account(
    id: Long,
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    creationDate: Timestamp,
    userType: String
)

case class PublicAccount(
    id: Long,
    email: String,
    firstName: String,
    lastName: String,
    userType: String,
    creationDate: Timestamp
)

object TimestampFormats {
  def timestampToDateTime(t: Timestamp): LocalDateTime = t.toLocalDateTime

  def dateTimeToTimestamp(ldt: LocalDateTime): Timestamp = Timestamp.valueOf(ldt)

  implicit val timestampFormat = new Format[Timestamp] {
    def writes(t: Timestamp): JsValue = toJson(timestampToDateTime(t))

    def reads(json: JsValue): JsResult[Timestamp] = fromJson[LocalDateTime](json).map(dateTimeToTimestamp)
  }
}

object Account {
  import TimestampFormats._

  implicit val accountFormat = Json.format[Account]
}

object PublicAccount {
  import TimestampFormats._

  def from(account: Account) =
    PublicAccount(account.id, account.email, account.firstName, account.lastName, account.userType, account.creationDate)

  implicit val publicAccountFormat = Json.format[PublicAccount]
}
