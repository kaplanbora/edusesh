package repositories

import java.sql.Timestamp
import javax.inject._

import models.Transaction
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent._

@Singleton
class TransactionRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._

  private class TransactionTable(tag: Tag) extends Table[Transaction](tag, "Transaction") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def lessonId = column[Long]("lesson_id")

    def studentId = column[Long]("student_id")

    def price = column[Double]("price")

    def date = column[Timestamp]("date")

    def * = (id, lessonId, studentId, price, date) <> ((Transaction.apply _).tupled, Transaction.unapply)
  }

  private val transactions = TableQuery[TransactionTable]

  private val transactionById = Compiled((id: Rep[Long]) => transactions.filter(_.id === id))

  def list(): Future[Seq[Transaction]] = db.run {
    transactions.result
  }

  def get(id: Long): Future[Seq[Transaction]] = db.run {
    transactionById(id).result
  }

  def create(transaction: Transaction): Future[Int] = db.run {
    transactions += transaction
  }

  def update(id: Long, transaction: Transaction): Future[Int] = db.run {
    transactionById(id).update(transaction).transactionally
  }

  def delete(id: Long): Future[Int] = db.run {
    transactionById(id).delete
  }
}