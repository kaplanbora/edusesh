package models

import play.api.libs.json.{Format, Json, Reads, Writes}
import com.github.nscala_time.time.Imports._

case class Review(id: Long,
                  lesson: Lesson,
                  student: Account,
                  rating: Double,
                  date: DateTime,
                  comment: String)

object Review {
  val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val dateFormat = Format[DateTime](Reads.jodaDateReads(pattern), Writes.jodaDateWrites(pattern))
  implicit val reviewFormat = Json.format[Review]
}

