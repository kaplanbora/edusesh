package models

import play.api.libs.json.{Format, Json, Reads, Writes}
import com.github.nscala_time.time.Imports._

case class Report(id: Long,
                  lesson: Lesson,
                  student: Account,
                  incident: String,
                  date: DateTime,
                  description: String)

object Report {
  val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val dateFormat = Format[DateTime](Reads.jodaDateReads(pattern), Writes.jodaDateWrites(pattern))
  implicit val reportFormat = Json.format[Report]
}

