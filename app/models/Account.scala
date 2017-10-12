package models

import java.sql.Timestamp
import java.time.LocalDateTime

import play.api.libs.json._
import play.api.libs.json.Json._

//sealed trait Role
//
//case object Student extends Role
//
//case object Teacher extends Role

case class Account(id: Long,
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    creationDate: Timestamp,
    userType: String)

object Account {

//  implicit val roleFormat = new Format[Role] {
//    def writes(role: Role): JsValue = role match {
//      case Student => toJson("student")
//      case Teacher => toJson("teacher")
//    }
//
//    def reads(json: JsValue): JsResult[Role] = fromJson[Role](json).map {
//      case
//    }
//  }

  def timestampToDateTime(t: Timestamp): LocalDateTime = t.toLocalDateTime

  def dateTimeToTimestamp(ldt: LocalDateTime): Timestamp = Timestamp.valueOf(ldt)

  implicit val timestampFormat = new Format[Timestamp] {
    def writes(t: Timestamp): JsValue = toJson(timestampToDateTime(t))

    def reads(json: JsValue): JsResult[Timestamp] = fromJson[LocalDateTime](json).map(dateTimeToTimestamp)
  }

  implicit val accountFormat = Json.format[Account]
}
