package models

import java.time.LocalDateTime

import play.api.libs.json.Json

case class Message(
    id: Long,
    senderId: Long,
    receiverId: Long,
    conversationId: Long,
    body: String,
    date: LocalDateTime
)

object Message {
  import forms.TimestampFormats._

  implicit val messageFormat = Json.format[Message]
}
