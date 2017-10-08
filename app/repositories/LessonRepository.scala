package repositories

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import models.Lesson
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LessonRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._

  private class LessonTable(tag: Tag) extends Table[Lesson](tag, "Lesson") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def teacherId = column[Long]("teacher_id")

    def categoryId = column[Long]("category_id")

    def name = column[String]("name")

    def price = column[Double]("price")

    def creationDate = column[Timestamp]("creationDate")

    def description = column[String]("description")

    def isActive = column[Boolean]("isActive")

    def * = (id, teacherId, categoryId, name, price, creationDate, description, isActive) <>
      ((Lesson.apply _).tupled, Lesson.unapply)

  }

  private val lessons = TableQuery[LessonTable]

  def list(): Future[Seq[Lesson]] = db.run {
    lessons.result
  }

  def get(id: Long): Future[Option[Lesson]] = db.run {
    lessons
      .filter(_.id === id)
      .result
      .headOption
  }

  def create(
      teacherId: Long,
      categoryId: Long,
      name: String,
      price: Double,
      creationDate: Timestamp,
      description: String,
      isActive: Boolean
  ): Future[Lesson] = db.run {
    (lessons.map(l => (l.teacherId, l.categoryId, l.name, l.price, l.creationDate, l.description, l.isActive))
      returning lessons.map(_.id)
      into ((vals, id) => Lesson(id, vals._1, vals._2, vals._3, vals._4, vals._5, vals._6, vals._7))
      ) += (teacherId, categoryId, name, price, creationDate, description, isActive)
  }

  def update(
      id: Long,
      teacherId: Long,
      categoryId: Long,
      name: String,
      price: Double,
      creationDate: Timestamp,
      description: String,
      isActive: Boolean
  ): Future[Int] = db.run {
    lessons
      .filter(_.id === id)
      .map(l => (l.teacherId, l.categoryId, l.name, l.price, l.creationDate, l.description, l.isActive))
      .update((teacherId, categoryId, name, price, creationDate, description, isActive))
      .transactionally
  }

  def delete(id: Long): Future[Int] = db.run {
    lessons
      .filter(_.id === id)
      .delete
  }
}
