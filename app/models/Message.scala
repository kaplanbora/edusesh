package models

import java.time.LocalDateTime

import play.api.libs.json.Json
import util.DateFormats

case class Message(
    id: Long,
    senderId: Long,
    receiverId: Long,
    conversationId: Long,
    body: String,
    date: LocalDateTime
)

object Message extends DateFormats {
  implicit val messageFormat = Json.format[Message]
}
