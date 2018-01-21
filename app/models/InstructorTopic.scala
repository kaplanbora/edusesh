package models

import play.api.libs.json.Json

case class InstructorTopic(id: Long, instructorId: Long, topicId: Long)

object InstructorTopic {
  implicit val instructorTopicFormat = Json.format[InstructorTopic]
}
