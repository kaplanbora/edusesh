package models

import play.api.libs.json.Json

case class UserTopic(id: Long, name: String, parentId: Long)

object UserTopic {
  implicit val topicFormat = Json.format[UserTopic]
}

