package daos

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile
import models._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] with DbMappings {

  import profile.api._

  private class SessionTable(tag: Tag) extends Table[LiveSession](tag, "sessions") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def traineeId = column[Long]("trainee_id")
    def instructorId = column[Long]("instructor_id")
    def topicId = column[Long]("topic_id")
    def date = column[LocalDateTime]("date")
    def isApproved = column[Boolean]("is_approved")
    def isCompleted = column[Boolean]("is_completed")

    def * = (id, name, traineeId, instructorId, topicId, date, isApproved, isCompleted) <>
      ((LiveSession.apply _).tupled, LiveSession.unapply)
  }

  private class ReportTable(tag: Tag) extends Table[Report](tag, "reports") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sessionId = column[Long]("session_id")
    def userId = column[Long]("user_id")
    def title = column[String]("name")
    def description = column[String]("description")
    def isResolved = column[Boolean]("is_resolved")
    def date = column[LocalDateTime]("date")

    def session = foreignKey("reportsSessionsFK", sessionId, liveSessions)(_.id)

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

    def session = foreignKey("reviewSessionsFK", sessionId, liveSessions)(_.id)

    def * = (id, sessionId, traineeId, rating, title, comment, date) <>
      ((Review.apply _).tupled, Review.unapply)
  }

  private class SessionFileTable(tag: Tag) extends Table[SessionFile](tag, "session_files") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sessionId = column[Long]("session_id")
    def name = column[String]("name")
    def link = column[String]("link")

    def session = foreignKey("sessionFilesFK", sessionId, liveSessions)(_.id)

    def * = (id, sessionId, name, link) <>
      ((SessionFile.apply _).tupled, SessionFile.unapply)
  }

  private val liveSessions = TableQuery[SessionTable]
  private val sessionFiles = TableQuery[SessionFileTable]
  private val reports = TableQuery[ReportTable]
  private val reviews = TableQuery[ReviewTable]

  def getSession(id: Long): Future[Option[LiveSession]] = db.run {
    liveSessions.filter(_.id === id)
      .result
      .headOption
  }

  def getSessionsForUser(userId: Long): Future[Seq[LiveSession]] = db.run {
    liveSessions.filter(session => session.instructorId === userId || session.traineeId === userId)
      .result
  }

  def getReport(id: Long): Future[Option[Report]] = db.run {
    reports.filter(_.id === id)
      .result
      .headOption
  }

  def getReview(id: Long): Future[Option[Review]] = db.run {
    reviews.filter(_.id === id)
      .result
      .headOption
  }

  def getFilesForSession(sessionId: Long): Future[Seq[SessionFile]] = db.run {
    sessionFiles.filter(_.sessionId === sessionId)
      .result
  }

  def getReportsForUser(userId: Long): Future[Seq[Report]] = db.run {
    reports.filter(_.userId === userId)
      .result
  }

  def getReviewsForInstructor(instructorId: Long): Future[Seq[Review]] = db.run {
    val sessionIds = liveSessions.filter(_.instructorId === instructorId).map(_.id)
    reviews.filter(_.sessionId in sessionIds)
      .result
  }

  def getReviewsForTrainee(traineeId: Long): Future[Seq[Review]] = db.run {
    reviews.filter(_.traineeId === traineeId)
      .result
  }

  def getReviewForSession(traineeId: Long, sessionId: Long): Future[Option[Review]] = db.run {
    reviews.filter(review => review.traineeId === traineeId && review.sessionId === sessionId)
      .result
      .headOption
  }

  def createSession(form: SessionForm): Future[Long] = db.run {
    (liveSessions returning liveSessions.map(_.id)) +=
      LiveSession(-1, form.name, form.traineeId, form.instructorId, form.topicId, form.date, form.isApproved, form.isCompleted)
  }
}
