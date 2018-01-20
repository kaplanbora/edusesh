package models

import play.api.libs.json.Json

case class SessionFile(id: Long, sessionId: Long, name: String, link: String)

object SessionFile {
  implicit val sessionFileFormat = Json.format[SessionFile]
}
