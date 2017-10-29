package models

import java.sql.Timestamp
import java.time.LocalDateTime

import play.api.libs.json._
import play.api.libs.json.Json._

case class Review(
    id: Long,
    lessonId: Long,
    studentId: Long,
    rating: Double,
    date: Timestamp,
    title: String,
    comment: String)

object Review {
  def timestampToDateTime(t: Timestamp): LocalDateTime = t.toLocalDateTime

  def dateTimeToTimestamp(ldt: LocalDateTime): Timestamp = Timestamp.valueOf(ldt)

  implicit val timestampFormat = new Format[Timestamp] {
    def writes(t: Timestamp): JsValue = toJson(timestampToDateTime(t))

    def reads(json: JsValue): JsResult[Timestamp] = fromJson[LocalDateTime](json).map(dateTimeToTimestamp)
  }

  implicit val reviewFormat = Json.format[Review]
}

