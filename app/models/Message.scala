package models

import com.github.nscala_time.time.Imports._
import play.api.libs.json.{Format, Json, Reads, Writes}

case class Message(id: Long,
                   sender: Account,
                   receiver: Account,
                   body: String,
                   date: DateTime)

object Message {
  val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val dateFormat = Format[DateTime](Reads.jodaDateReads(pattern), Writes.jodaDateWrites(pattern))
  implicit val messageFormat = Json.format[Message]
}

