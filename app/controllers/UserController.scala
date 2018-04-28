package controllers

import java.time.LocalDateTime
import javax.inject.Inject

import actions._
import auth.{Security, Token}
import forms._
import forms.UserForms._
import models._
import models.InstructorProfile._
import play.api.libs.json._
import play.api.mvc._
import daos._

import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(
    chatDao: ChatDAO,
    userDao: UserDAO,
    sessionDao: SessionDAO,
    topicDao: TopicDAO,
    instructorAction: InstructorAction,
    traineeAction: TraineeAction,
    authAction: AuthenticatedAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def currentTime = LocalDateTime.now()


  def login = Action(parse.json).async { implicit request =>
    request.body.validate[UserCredentialsForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      credentials => {
        userDao.getCredentialsByEmail(credentials.email).map {
          case Some(user) if Security.checkPassword(credentials.password, user) =>
            Ok(Json.obj("token" -> Token.generate(user)))
          case _ => BadRequest(Json.obj("error" -> "No match for this email and password."))
        }
      }
    )
  }

  def register(role: String) = Action(parse.json).async { implicit request =>
    role match {
      case "instructor" => registerInstructor(request.body)
      case "trainee" => registerTrainee(request.body)
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Incorrect user role.")))
    }
  }

  def checkEmail = Action(parse.json).async { implicit request =>
    request.body.validate[CheckEmailForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      form => {
        userDao.emailExists(form.email)
          .map(exists => Ok(Json.obj("emailExists" -> exists)))
      }
    )
  }

  def registerTrainee(body: JsValue): Future[Result] = {
    body.validate[UserCredentialsForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      form => {
        val newUser = for {
          userId <- userDao.createUser(form, TraineeRole, currentTime)
          _ <- userDao.createTraineeProfile(userId)
          newUser <- userDao.getCredentialsById(userId)
        } yield newUser
        newUser.map {
          case Some(user) => Created(Json.obj("token" -> Token.generate(user)))
          case None => BadRequest(Json.obj("error" -> "Creation failed."))
        }
      }
    )
  }

  def registerInstructor(body: JsValue): Future[Result] = {
    body.validate[UserCredentialsForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      form => {
        val newUser = for {
          userId <- userDao.createUser(form, InstructorRole, currentTime)
          _ <- userDao.createInstructorProfile(userId)
          newUser <- userDao.getCredentialsById(userId)
        } yield newUser
        newUser.map {
          case Some(user) => Created(Json.obj("token" -> Token.generate(user)))
          case None => BadRequest(Json.obj("error" -> "Creation failed."))
        }
      }
    )
  }

  def getSessions = authAction.async { implicit request =>
    sessionDao.getSessionsForUser(request.credentials.id)
      .map(sessions => Ok(Json.toJson(sessions)))
  }

  def getReports = authAction.async { implicit request =>
    sessionDao.getReportsForUser(request.credentials.id)
      .map(reports => Ok(Json.toJson(reports)))
  }

  def getReviews = authAction.async { implicit request =>
    request.credentials.userRole match {
      case InstructorRole => sessionDao.getReviewsForInstructor(request.credentials.id)
        .map(reviews => Ok(Json.toJson(reviews)))
      case TraineeRole => sessionDao.getReviewsForInstructor(request.credentials.id)
        .map(reviews => Ok(Json.toJson(reviews)))
      case _ => Future.successful(NotFound(Json.obj("error" -> "Unknown user role.")))
    }
  }

  // If a user deleted this conversation don't send the conversation to them
  def getConversations = authAction.async { implicit request =>
    chatDao.getConversationsForUser(request.credentials.id).map(conversations => {
      Ok(Json.toJson(
        conversations.filter(conv => (conv.userId1, conv.userId2) match {
          case (request.credentials.id, _) if conv.userRemoved1 => false
          case (_, request.credentials.id) if conv.userRemoved2 => false
          case _ => true
        })))
    })
  }

  def average(s: Seq[Double]): Double = {
    val len = s.length
    if (len > 0) s.sum / len else 0
  }

  def mkName(first: Option[String], last: Option[String]): String =
    first -> last match {
      case (Some(f), Some(l)) => s"$f $l"
      case _ => ""
    }

  case class SearchResult(userId: Long, name: String, hourlyRate: Double, occupation: String, rating: Double, sessionCount: Int)

  implicit val searchResultFormat = Json.format[SearchResult]

  def mkSearchResult(instructorProfile: Option[InstructorProfile]): Future[Option[SearchResult]] =
    instructorProfile match {
      case Some(profile) =>
        for {
          sessions <- sessionDao.getSessionsForUser(profile.userId).map(_.filter(_.isCompleted))
          ratings <- sessionDao.getReviewsForInstructor(profile.userId).map(_.map(_.rating))
        } yield Some(
          SearchResult(
            profile.userId,
            mkName(profile.firstName, profile.lastName),
            profile.hourlyRate,
            profile.occupation.getOrElse("Unemployed"),
            average(ratings),
            sessions.length)
        )
      case None => Future.successful(None)
    }

  def search(category: String, query: Option[String]) = Action.async { implicit request =>
    (category, query) match {
      case ("topic", Some(q)) =>
        topicDao.getInstructorIdsForTopic(q).flatMap(instructorIds => {
          val searchResults = instructorIds.map(id => {
            userDao.getInstructorProfile(id).flatMap(mkSearchResult)
          })
          Future.sequence(searchResults).map(results => Ok(Json.toJson(results)))
        })
      case ("instructor", Some(q)) =>
        userDao.getInstructorsByName(q)
          .flatMap(instructors => {
            val searchResults = instructors.map(instructor => mkSearchResult(Some(instructor)))
            Future.sequence(searchResults).map(results => Ok(Json.toJson(results)))
          })
      case _ =>
        userDao.listInstructorProfiles
          .flatMap(instructors => {
            val searchResults = instructors.map(instructor => mkSearchResult(Some(instructor)))
            Future.sequence(searchResults).map(results => Ok(Json.toJson(results)))         
          })
    }
  }

  def getSelfCredentials = authAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(request.credentials)))
  }

  def getCredentials(id: Long) = Action.async { implicit request =>
    userDao.getCredentialsById(id).map {
      case Some(credentials) => Ok(Json.toJson(credentials))
      case None => BadRequest(Json.obj("error" -> "User not found."))
    }
  }

  def getSelfProfile = authAction.async { implicit request =>
    request.credentials.userRole match {
      case InstructorRole => userDao.getInstructorProfile(request.credentials.id)
        .map(instructor => Ok(Json.toJson(instructor)))
      case TraineeRole => userDao.getTraineeProfile(request.credentials.id)
        .map(trainee => Ok(Json.toJson(trainee)))
      case _ => Future.successful(NotFound(Json.obj("error" -> "Unknown user role.")))
    }
  }

  def getProfile(id: Long) = Action.async { implicit request =>
    userDao.getCredentialsById(id).flatMap {
      case Some(user) => user.userRole match {
        case InstructorRole => userDao.getInstructorProfile(id).map(inst => Ok(Json.toJson(inst)))
        case TraineeRole => userDao.getTraineeProfile(id).map(trai => Ok(Json.toJson(trai)))
        case _ => Future.successful(NotFound(Json.obj("error" -> "Unknown user role.")))
      }
      case None => Future.successful(NotFound(Json.obj("error" -> "User not found.")))
    }
  }

  def updateProfile = authAction(parse.json).async { implicit request =>
    request.credentials.userRole match {
      case InstructorRole => updateInstructorProfile(request.body, request.credentials.id)
      case TraineeRole => updateTraineeProfile(request.body, request.credentials.id)
      case _ => Future.successful(NotFound(Json.obj("error" -> "Unknown user role.")))
    }
  }

  def updateInstructorProfile(body: JsValue, userId: Long): Future[Result] = {
    body.validate[InstructorProfileForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      profile => {
        userDao.updateInstructorProfile(userId, profile)
          .map(lines => Ok(Json.obj("updated" -> lines)))
      }
    )
  }

  def updateTraineeProfile(body: JsValue, userId: Long): Future[Result] = {
    body.validate[TraineeProfileForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      profile => {
        userDao.updateTraineeProfile(userId, profile)
          .map(lines => Ok(Json.obj("updated" -> lines)))
      }
    )
  }

  def updateCredentials = authAction(parse.json).async { implicit request =>
    request.body.validate[UserCredentialsForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      credentials => {
        userDao.updateCredentials(request.credentials.id, credentials)
          .map(lines => Ok(Json.obj("updated" -> lines)))
      }
    )
  }

  def delete = authAction.async { implicit request =>
    userDao.deleteUser(request.credentials.id)
      .map(lines => Ok(Json.obj("deleted" -> lines)))
  }

  def headers = List(
    "Access-Control-Max-Age" -> "3600",
    "Access-Control-Allow-Headers" -> "Origin, Content-Type, Accept, Authorization, JWT",
    "Access-Control-Allow-Credentials" -> "true"
  )

  def rootOptions = options("/")

  def options(url: String) = Action { request =>
    NoContent.withHeaders(headers: _*)
  }
}

