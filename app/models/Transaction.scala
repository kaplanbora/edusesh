package models

import com.github.nscala_time.time.Imports._
import play.api.libs.json.{Format, Json, Reads, Writes}

case class Transaction(id: Long,
                       lesson: Lesson,
                       student: Account,
                       date: DateTime)

object Transaction {
  val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val dateFormat = Format[DateTime](Reads.jodaDateReads(pattern), Writes.jodaDateWrites(pattern))
  implicit val transactionFormat = Json.format[Transaction]
}
