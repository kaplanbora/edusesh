package repositories

import javax.inject.{Inject, Singleton}

import models.Category
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class CategoryTable(tag: Tag) extends Table[Category](tag, "category") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id, name) <> ((Category.apply _).tupled, Category.unapply)

    private val categories = TableQuery[CategoryTable]

    /**
      * List all categories
      *
      * @return
      */
    def list(): Future[Seq[Category]] = db.run {
      categories.result
    }

    def create(name: String): Future[Category] = db.run {
      (categories.map(c => c.name)
        returning categories.map(_.id)
        into ((name, id) => Category(id, name))
        ) += name
    }
  }

}
