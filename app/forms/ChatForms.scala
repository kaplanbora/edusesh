package forms

import play.api.libs.json.Json

case class ConversationForm(userId1: Long, userId2: Long, userRemoved1: Boolean, userRemoved2: Boolean)
case class MessageForm(senderId: Long, receiverId: Long, body: String)

object ChatForms {
  implicit val conversationFormFormat = Json.format[ConversationForm]
  implicit val messageFormFormat = Json.format[MessageForm]
}
