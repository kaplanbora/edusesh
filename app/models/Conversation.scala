package models

import play.api.libs.json.Json

case class Conversation(id: Long, userId1: Long, userId2: Long, userRemoved1: Boolean, userRemoved2: Boolean)

object Conversation {
  implicit val conversationFormat = Json.format[Conversation]
}
