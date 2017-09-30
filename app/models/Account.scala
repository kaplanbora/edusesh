package models

import play.api.libs.json.{Format, Json, Reads, Writes}
import com.github.nscala_time.time.Imports._

case class Account(id: Long,
                   email: String,
                   password: String,
                   firstName: String,
                   lastName: String,
                   creationDate: DateTime,
                   userType: String)

object Account {
  val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val dateFormat = Format[DateTime](Reads.jodaDateReads(pattern), Writes.jodaDateWrites(pattern))
  implicit val accountFormat = Json.format[Account]
}
