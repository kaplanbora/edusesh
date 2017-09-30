package models

import com.github.nscala_money.money.Imports._
import com.github.nscala_time.time.Imports._
import play.api.libs.json.{Format, Json, Reads, Writes}

import scala.concurrent.duration.Duration

// Price should be type Money and duration should be type Duration
case class Lesson(id: Long,
                  teacher: Account,
                  price: Int,
                  category: Category,
                  duration: Int,
                  creationDate: DateTime,
                  description: String,
                  isActive: Boolean)

object Lesson {
  val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  Money.formatted("")
  implicit val dateFormat = Format[DateTime](Reads.jodaDateReads(pattern), Writes.jodaDateWrites(pattern))
  implicit val lessonFormat = Json.format[Lesson]
}