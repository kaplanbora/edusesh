package models

import java.time.LocalDateTime

import forms.DateFormats
import play.api.libs.json.Json

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
