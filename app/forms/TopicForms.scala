package forms

import play.api.libs.json.Json

case class UserTopicForm(name: String, parentId: Long)

object TopicForms {
  implicit val userTopicFormFormat = Json.format[UserTopicForm]

}
