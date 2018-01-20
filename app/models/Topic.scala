package models

import play.api.libs.json.Json

case class Topic(id: Long, name: String, parentId: Long)

object Topic {
  implicit val topicFormat = Json.format[Topic]
}

