package models

import play.api.libs.json.Json

case class MainTopic(id: Long, name: String)

object MainTopic {
  implicit val mainTopicFormat = Json.format[MainTopic]
}