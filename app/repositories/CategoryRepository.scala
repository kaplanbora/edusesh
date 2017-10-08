package repositories

import javax.inject.{Inject, Singleton}

import models.Category
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._

  private class CategoryTable(tag: Tag) extends Table[Category](tag, "Category") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def * = (id, name) <> ((Category.apply _).tupled, Category.unapply)
  }

  private val categories = TableQuery[CategoryTable]

  def list(): Future[Seq[Category]] = db.run {
    categories.result
  }

  def get(id: Long): Future[Option[Category]] = db.run {
    categories
      .filter(_.id === id)
      .result
      .headOption
  }

  def create(name: String): Future[Category] = db.run {
    (categories.map(c => c.name)
      returning categories.map(_.id)
      into ((name, id) => Category(id, name))
      ) += name
  }

  def update(id: Long, name: String): Future[Int] = db.run {
    categories
      .filter(_.id === id)
      .map(_.name)
      .update(name)
  }
}
