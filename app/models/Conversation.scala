package models

import play.api.libs.json.Json

case class Conversation(id: Long, userId1: Long, userId2: Long)

object Conversation {
  implicit val conversationFormat = Json.format[Conversation]
}
