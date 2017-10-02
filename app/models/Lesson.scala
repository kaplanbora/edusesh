package models

import java.time.LocalDateTime
import java.sql.Timestamp

import play.api.libs.json._
import play.api.libs.json.Json._


// Price should be type Money and duration should be type Duration
case class Lesson(id: Long,
                  teacher: Account,
                  category: Category,
                  name: String,
                  price: Int,
                  duration: Int,
                  creationDate: Timestamp,
                  description: String,
                  isActive: Boolean)

object Lesson {
  def timestampToDateTime(t: Timestamp): LocalDateTime = t.toLocalDateTime

  def dateTimeToTimestamp(ldt: LocalDateTime): Timestamp = Timestamp.valueOf(ldt)

  implicit val timestampFormat = new Format[Timestamp] {
    def writes(t: Timestamp): JsValue = toJson(timestampToDateTime(t))

    def reads(json: JsValue): JsResult[Timestamp] = fromJson[LocalDateTime](json).map(dateTimeToTimestamp)
  }

  implicit val lessonFormat = Json.format[Lesson]
}