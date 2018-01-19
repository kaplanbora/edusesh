package repositories

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import forms._
import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._

  implicit val userRoleMap = MappedColumnType.base[UserRole, String](
    {
      case AdminRole => "admin"
      case InstructorRole => "instructor"
      case TraineeRole => "trainee"
    },
    {
      case "admin" => AdminRole
      case "instructor" => InstructorRole
      case "trainee" => TraineeRole
    }
  )

  implicit val timestampMap = MappedColumnType.base[LocalDateTime, Timestamp](
    ldt => Timestamp.valueOf(ldt),
    ts => ts.toLocalDateTime
  )

  private class UserCredentialsTable(tag: Tag) extends Table[UserCredentials](tag, "user_credentials") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email", O.Unique)
    def password = column[String]("password")
    def creationDate = column[LocalDateTime]("creation_date")
    def userRole = column[UserRole]("user_role")

    def * = (id, email, password, creationDate, userRole) <>
      ((UserCredentials.apply _).tupled, UserCredentials.unapply)
  }

  private class TraineeProfilesTable(tag: Tag) extends Table[TraineeProfile](tag, "trainee_profiles") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[Option[String]]("first_name")
    def lastName = column[Option[String]]("last_name")
    def imageLink = column[Option[String]]("image_link")
    def userId = column[Long]("user_id", O.Unique)

    def * = (id, firstName, lastName, imageLink, userId) <>
      ((TraineeProfile.apply _).tupled, TraineeProfile.unapply)
  }

  private class InstructorProfileTable(tag: Tag) extends Table[InstructorProfile](tag, "instructor_profiles") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[Option[String]]("first_name")
    def lastName = column[Option[String]]("last_name")
    def description = column[Option[String]]("description")
    def occupation = column[Option[String]]("occupation")
    def imageLink = column[Option[String]]("image_link")
    def videoLink = column[Option[String]]("video_link")
    def hourlyRate = column[Double]("hourly_rate")
    def userId = column[Long]("user_id", O.Unique)

    def * = (id, firstName, lastName, description, occupation, imageLink, videoLink, hourlyRate, userId) <>
      ((InstructorProfile.apply _).tupled, InstructorProfile.unapply)
  }

  private val userCredentials = TableQuery[UserCredentialsTable]
  private val traineeProfiles = TableQuery[TraineeProfilesTable]
  private val instructorProfiles = TableQuery[InstructorProfileTable]

  def listCredentials(): Future[Seq[UserCredentials]] = db.run {
    userCredentials.result
  }

  def getCredentialsByEmail(email: String): Future[Option[UserCredentials]] = db.run {
    userCredentials
      .filter(_.email === email)
      .result
      .headOption
  }

  def getCredentialsById(id: Long): Future[Option[UserCredentials]] = db.run {
    userCredentials
      .filter(_.id === id)
      .result
      .headOption
  }

  def getInstructorProfile(userId: Long): Future[Option[InstructorProfile]] = db.run {
    instructorProfiles
      .filter(_.userId === userId)
      .result
      .headOption
  }

  def getTraineeProfile(userId: Long): Future[Option[TraineeProfile]] = db.run {
    traineeProfiles
      .filter(_.userId === userId)
      .result
      .headOption
  }

  def emailExists(email: String): Future[Boolean] = db.run {
    userCredentials
      .filter(_.email === email)
      .exists
      .result
  }

  def createUser(form: UserRegisterForm, creationDate: LocalDateTime): Future[Long] = db.run {
    (userCredentials returning userCredentials.map(_.id)) +=
      UserCredentials(-1, form.email, form.password, creationDate, form.userRole)
    // In controller:
    //    form.userRole match {
    //      case InstructorRole => createInstructorProfile(userId, creationDate)
    //      case TraineeRole => createTraineeProfile(userId, creationDate)
    //    }
  }

  def createInstructorProfile(userId: Long, creationDate: LocalDateTime): Future[Long] = db.run {
    (instructorProfiles returning instructorProfiles.map(_.id)) +=
      InstructorProfile(-1, None, None, None, None, None, None, 0, userId)
  }

  def createTraineeProfile(userId: Long, creationDate: LocalDateTime): Future[Long] = db.run {
    (traineeProfiles returning traineeProfiles.map(_.id)) +=
      TraineeProfile(-1, None, None, None, userId)
  }

  def updateCredentials(userId: Long, form: UserCredentialsForm): Future[Int] = db.run {
    userCredentials.filter(_.id === userId)
      .map(credentials => (credentials.email, credentials.password))
      .update((form.email, form.password))
      .transactionally
  }

  def updateInstructorProfile(userId: Long, form: InstructorProfileForm): Future[Int] = db.run {
    instructorProfiles.filter(_.userId === userId)
      .map(profile => (profile.firstName, profile.lastName, profile.description, profile.occupation, profile.imageLink, profile.videoLink, profile.hourlyRate))
      .update((form.firstName, form.lastName, form.description, form.occupation, form.imageLink, form.videoLink, form.hourlyRate))
      .transactionally
  }

  def updateTraineeProfile(userId: Long, form: TraineeProfileForm): Future[Int] = db.run {
    traineeProfiles.filter(_.userId === userId)
      .map(profile => (profile.firstName, profile.lastName, profile.imageLink))
      .update((form.firstName, form.lastName, form.imageLink))
      .transactionally
  }

  def deleteUser(id: Long): Future[Int] = db.run {
    userCredentials.filter(_.id === id)
      .delete
      .transactionally
  }
}
