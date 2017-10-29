package repositories

import javax.inject.{Inject, Singleton}

import models.Account
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}
import java.sql.Timestamp

@Singleton
class AccountRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._

  private class AccountTable(tag: Tag) extends Table[Account](tag, "Account") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email", O.Unique)

    def password = column[String]("password")

    def firstName = column[String]("first_name")

    def lastName = column[String]("last_name")

    def creationDate = column[Timestamp]("creation_date")

    def userType = column[String]("user_type")

    def * = (id, email, password, firstName, lastName, creationDate, userType) <>
      ((Account.apply _).tupled, Account.unapply)
  }

  private val accounts = TableQuery[AccountTable]

  def list(): Future[Seq[Account]] = db.run {
    accounts.result
  }

  def create(email: String, password: String, firstName: String,
      lastName: String, creationDate: Timestamp, userType: String): Future[Account] = db.run {
    (accounts.map(a => (a.email, a.password, a.firstName, a.lastName, a.creationDate, a.userType))
      returning accounts.map(_.id)
      into ((values, id) => Account(id, values._1, values._2, values._3, values._4, values._5, values._6))
      ) += (email, password, firstName, lastName, creationDate, userType)
  }

  def update(id: Long, email: String, password: String, firstName: String, lastName: String): Future[Int] = db.run {
    accounts
      .filter(_.id === id)
      .map(a => (a.email, a.password, a.firstName, a.lastName))
      .update((email, password, firstName, lastName))
      .transactionally
  }

  def delete(id: Long): Future[Int] = db.run {
    accounts
      .filter(_.id === id)
      .delete
  }

  def byEmail(email: String): Future[Option[Account]] = db.run {
    accounts
      .filter(_.email === email)
      .result
      .headOption
  }
}
