package forms

import play.api.libs.json.Json

case class UserTopicForm(name: String, parentId: Long)
case class InstructorTopicForm(instructorId: Long, topicId: Long)

object TopicForms {
  implicit val userTopicFormFormat = Json.format[UserTopicForm]
  implicit val instructorTopicFormat = Json.format[InstructorTopicForm]
}
