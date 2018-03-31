package daos

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile
import models._
import util.DbMappings

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] with DbMappings {

  import profile.api._

  private class SessionTable(tag: Tag) extends Table[LiveSession](tag, "sessions") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def traineeId = column[Long]("trainee_id")
    def instructorId = column[Long]("instructor_id")
    def topicId = column[Long]("topic_id")
    def date = column[LocalDateTime]("date")
    def isApproved = column[Boolean]("is_approved")
    def isCompleted = column[Boolean]("is_completed")
    def isDeleted = column[Boolean]("is_deleted")

    def * = (id, name, description, traineeId, instructorId, topicId, date, isApproved, isCompleted, isDeleted) <>
      ((LiveSession.apply _).tupled, LiveSession.unapply)
  }

  private class ReportTable(tag: Tag) extends Table[Report](tag, "reports") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sessionId = column[Long]("session_id")
    def userId = column[Long]("user_id")
    def title = column[String]("title")
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
    def title = column[String]("title")
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

  def createSession(traineeId: Long, form: SessionForm): Future[Long] = db.run {
    (liveSessions returning liveSessions.map(_.id)) +=
      LiveSession(-1, form.name, form.description, traineeId, form.instructorId, form.topicId, form.date, false, false, false)
  }

  def createReport(traineeId: Long, sessionId: Long, form: ReportForm, date: LocalDateTime): Future[Long] = db.run {
    (reports returning reports.map(_.id)) +=
      Report(-1, sessionId, traineeId, form.title, form.description, false, date)
  }

  def createReview(traineeId: Long, sessionId: Long, form: ReviewForm, date: LocalDateTime): Future[Long] = db.run {
    (reviews returning reviews.map(_.id)) +=
      Review(-1, sessionId, traineeId, form.rating, form.title, form.comment, date)
  }

  def createSessionFile(sessionId: Long, form: SessionFileForm, date: LocalDateTime): Future[Long] = db.run {
    (sessionFiles returning sessionFiles.map(_.id)) +=
      SessionFile(-1, sessionId, form.name, form.link)
  }

  def updateSession(instructorId: Long, sessionId: Long, form: SessionUpdateForm): Future[Int] = db.run {
    liveSessions.filter(sesh => sesh.instructorId === instructorId && sesh.id === sessionId)
      .map(session => (session.isApproved, session.isCompleted))
      .update((form.isApproved, form.isCompleted))
      .transactionally
  }

  def deleteSession(sessionId: Long, userId: Long): Future[Int] = db.run {
    liveSessions.filter(_.id === sessionId)
      .filter(session => session.instructorId === userId || session.traineeId === userId)
      .map(_.isDeleted)
      .update(true)
      .transactionally
  }

  def updateReview(traineeId: Long, reviewId: Long, form: ReviewUpdateForm): Future[Int] = db.run {
    reviews.filter(review => review.id === reviewId && review.traineeId === traineeId)
      .map(review => (review.rating, review.title, review.comment))
      .update((form.rating, form.title, form.comment))
      .transactionally
  }

  def resolveReport(id: Long): Future[Int] = db.run {
    reports.filter(_.id === id)
      .map(_.isResolved)
      .update(true)
      .transactionally
  }

  def removeReview(traineeId: Long, reviewId: Long): Future[Int] = db.run {
    reviews.filter(review => review.id === reviewId && review.traineeId === traineeId)
      .delete
      .transactionally
  }

  def removeSessionFile(id: Long): Future[Int] = db.run {
    sessionFiles.filter(_.id === id)
      .delete
      .transactionally
  }
}
