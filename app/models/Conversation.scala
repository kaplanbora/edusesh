package models

import play.api.libs.json.Json

case class Conversation(id: Long, account1Id: Long, account2Id: Long)

object Conversation {
  implicit val conversationFormat = Json.format[Conversation]
}
