package repositories

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

abstract class ModelTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
}

trait Repository[C <: ModelTable[T], T] {
  protected val table: TableQuery[C]
  protected val db = Database.forConfig("")

  def list(): Future[Seq[C#TableElementType]] = db.run {
    table.result
  }

  def get(id: Long): Future[Option[C#TableElementType]] = db.run {
    table
      .filter(_.id === id)
      .result
      .headOption
  }

  def delete(id: Long): Future[Int]

  //  private val queryById = Compiled((id: Rep[Int]) => table.filter(_.id === id))
  //
  //  def all: Future[Seq[C#TableElementType]] = db.run(table.result)
  //
  //  def create(c: C#TableElementType): Future[Int] = db.run(table += c)
  //
  //  def read(id: Int): Future[Option[C#TableElementType]] = db.run(queryById(id).result.headOption)
  //
  //  def update(id: Int, c: C#TableElementType): Future[Int] = db.run(queryById(id).update(c))
  //
  //  def delete(id: Int): Future[Int] = db.run(queryById(id).delete)
}
