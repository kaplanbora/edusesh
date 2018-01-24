package daos

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile
import models._

import scala.concurrent.ExecutionContext

@Singleton
class SessionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] with DbMappings {

  import profile.api._

  private class SessionTable(tag: Tag) extends Table[models.Session](tag, "sessions") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def traineeId = column[Long]("trainee_id")
    def instructorId = column[Long]("instructor_id")
    def topicId = column[Long]("topic_id")
    def date = column[LocalDateTime]("date")
    def isApproved = column[Boolean]("is_approved")
    def isCompleted = column[Boolean]("is_completed")

    def * = (id, name, traineeId, instructorId, topicId, date, isApproved, isCompleted) <>
      ((Session.apply _).tupled, Session.unapply)
  }

  private class ReportTable(tag: Tag) extends Table[Report](tag, "reports") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sessionId = column[Long]("session_id")
    def userId = column[Long]("user_id")
    def title = column[String]("name")
    def description = column[String]("description")
    def isResolved = column[Boolean]("is_resolved")
    def date = column[LocalDateTime]("date")

    def * = (id, sessionId, userId, title, description, isResolved, date) <>
      ((Report.apply _).tupled, Report.unapply)
  }

  private class ReviewTable(tag: Tag) extends Table[Review](tag, "reviews") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sessionId = column[Long]("session_id")
    def traineeId = column[Long]("trainee_id")
    def rating = column[Double]("rating")
    def title = column[String]("name")
    def comment = column[Option[String]]("comment")
    def date = column[LocalDateTime]("date")

    def * = (id, sessionId, traineeId, rating, title, comment, date) <>
      ((Review.apply _).tupled, Review.unapply)
  }

  private class SessionFileTable(tag: Tag) extends Table[SessionFile](tag, "session_files") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sessionId = column[Long]("session_id")
    def name = column[String]("name")
    def link = column[String]("link")

    def * = (id, sessionId, name, link) <>
      ((SessionFile.apply _).tupled, SessionFile.unapply)
  }
}
