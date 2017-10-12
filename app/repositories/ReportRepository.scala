package repositories

import java.sql.Timestamp
import javax.inject._

import models.Report
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent._

@Singleton
class ReportRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._

  private class ReportTable(tag: Tag) extends Table[Report](tag, "Report") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def lessonId = column[Long]("lesson_id")

    def studentId = column[Long]("student_id")

    def title = column[String]("title")

    def date = column[Timestamp]("date")

    def isResolved = column[Boolean]("is_resolved")

    def description = column[String]("description")

    def * = (id, lessonId, studentId, title, date, isResolved, description) <> ((Report.apply _).tupled, Report.unapply)

  }

  private val reports = TableQuery[ReportTable]

  private val reportById = Compiled((id: Rep[Long]) => reports.filter(_.id === id))

  def list(): Future[Seq[Report]] = db.run {
    reports.result
  }

  def get(id: Long): Future[Seq[Report]] = db.run {
    reportById(id).result
  }

  def create(report: Report): Future[Int] = db.run {
    reports += report
  }

  def update(id: Long, report: Report): Future[Int] = db.run {
    reportById(id).update(report).transactionally
  }

  def delete(id: Long): Future[Int] = db.run {
    reportById(id).delete
  }
}
