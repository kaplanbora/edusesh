package forms

import play.api.libs.json.Json

case class UserTopicForm(name: String, parentId: Long)
case class InstructorTopicForm(instructorId: Long, topicId: Long)
case class SelfTopic(id: Long, name: String)

object TopicForms {
  implicit val userTopicFormFormat = Json.format[UserTopicForm]
  implicit val instructorTopicFormat = Json.format[InstructorTopicForm]
  implicit val selfTopicFormat = Json.format[SelfTopic]
}
