package models

import play.api.libs.json.Json

case class Conversation(id: Long, account1: Account, account2: Account)

object Conversation {
  implicit val conversationFormat = Json.format[Conversation]
}
