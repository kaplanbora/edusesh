package repositories

import java.sql.Timestamp
import javax.inject._

import models.Review
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent._

@Singleton
class ReviewRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._

  private class ReviewTable(tag: Tag) extends Table[Review](tag, "Review") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def lessonId = column[Long]("lesson_id")

    def studentId = column[Long]("student_id")

    def rating = column[Double]("rating")

    def date = column[Timestamp]("date")

    def title = column[String]("title")

    def comment = column[String]("title")

    def * = (id, lessonId, studentId, rating, date, title, comment) <> ((Review.apply _).tupled, Review.unapply)
  }

  private val reviews = TableQuery[ReviewTable]

  private val reviewById = Compiled((id: Rep[Long]) => reviews.filter(_.id === id))

  def list(): Future[Seq[Review]] = db.run {
    reviews.result
  }

  def get(id: Long): Future[Seq[Review]] = db.run {
    reviewById(id).result
  }

  def create(review: Review): Future[Int] = db.run {
    reviews += review
  }

  def update(id: Long, review: Review): Future[Int] = db.run {
    reviewById(id).update(review).transactionally
  }

  def delete(id: Long): Future[Int] = db.run {
    reviewById(id).delete
  }
}
