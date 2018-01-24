package util

import java.sql.Timestamp
import java.time.LocalDateTime

import play.api.libs.json.Json._
import play.api.libs.json._

trait DateFormats {
  def timestampToDateTime(t: Timestamp): LocalDateTime = t.toLocalDateTime
  def dateTimeToTimestamp(ldt: LocalDateTime): Timestamp = Timestamp.valueOf(ldt)

  implicit val timestampFormat = new Format[Timestamp] {
    def writes(t: Timestamp): JsValue = toJson(timestampToDateTime(t))
    def reads(json: JsValue): JsResult[Timestamp] = fromJson[LocalDateTime](json).map(dateTimeToTimestamp)
  }
}
