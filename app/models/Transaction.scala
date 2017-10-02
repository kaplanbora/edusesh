package models

import java.sql.Timestamp
import java.time.LocalDateTime

import play.api.libs.json._
import play.api.libs.json.Json._


case class Transaction(id: Long,
                       lesson: Lesson,
                       student: Account,
                       price: Int,
                       date: Timestamp)

object Transaction {
  def timestampToDateTime(t: Timestamp): LocalDateTime = t.toLocalDateTime

  def dateTimeToTimestamp(ldt: LocalDateTime): Timestamp = Timestamp.valueOf(ldt)

  implicit val timestampFormat = new Format[Timestamp] {
    def writes(t: Timestamp): JsValue = toJson(timestampToDateTime(t))

    def reads(json: JsValue): JsResult[Timestamp] = fromJson[LocalDateTime](json).map(dateTimeToTimestamp)
  }

  implicit val transactionFormat = Json.format[Transaction]
}