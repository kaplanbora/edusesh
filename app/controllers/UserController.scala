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
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(
    userRepo: UserRepository,
    instructorAction: InstructorAction,
    traineeAction: TraineeAction,
    authAction: AuthenticatedAction,
    cc: ControllerComponents)
  (implicit ec: ExecutionContext) extends AbstractController(cc) {

  def currentTime = LocalDateTime.now()

  def login = Action(parse.json).async { implicit request =>
    request.body.validate[UserCredentialsForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      credentials => {
        userRepo.getCredentialsByEmail(credentials.email).map {
          case Some(user) if Security.checkPassword(credentials.password, user) =>
            Ok(Json.obj("token" -> Token.generate(user)))
          case _ => BadRequest(Json.obj("error" -> "No match for this email and password."))
        }
      }
    )
  }

  def registerTrainee = Action(parse.json).async { implicit request =>
    request.body.validate[UserCredentialsForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      form => {
        val user = for {
          userId <- userRepo.createUser(form, TraineeRole, currentTime)
          _ <- userRepo.createTraineeProfile(userId)
          newUser <- userRepo.getCredentialsById(userId)
        } yield newUser
        user.map {
          case Some(u) => Created(Json.toJson(u))
          case None => BadRequest(Json.obj("error" -> "Creation failed."))
        }
      }
    )
  }

  def registerInstructor = Action(parse.json).async { implicit request =>
    request.body.validate[UserCredentialsForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      form => {
        val user = for {
          userId <- userRepo.createUser(form, TraineeRole, currentTime)
          _ <- userRepo.createInstructorProfile(userId)
          newUser <- userRepo.getCredentialsById(userId)
        } yield newUser
        user.map {
          case Some(u) => Created(Json.toJson(u))
          case None => BadRequest(Json.obj("error" -> "Creation failed."))
        }
      }
    )
  }

  def getOwnProfile = authAction.async { implicit request =>
    request.userRole match {
      case InstructorRole => userRepo.getInstructorProfile(request.userId)
          .map(instructor => Ok(Json.toJson(instructor)))
      case TraineeRole =>  userRepo.getTraineeProfile(request.userId)
          .map(trainee => Ok(Json.toJson(trainee)))
    }
  }

  def getProfile(id: Long) = Action.async { implicit request =>
    userRepo.getCredentialsById(id).flatMap {
      case Some(user) => user.userRole match {
        case InstructorRole => userRepo.getInstructorProfile(id).map(inst => Ok(Json.toJson(inst)))
        case TraineeRole => userRepo.getTraineeProfile(id).map(trai => Ok(Json.toJson(trai)))
      }
      case None => Future.successful(NotFound(Json.obj("error" -> "User not found.")))
    }
  }

  def updateInstructorProfile = instructorAction(parse.json).async { implicit request =>
    request.body.validate[InstructorProfileForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      profile => {
        userRepo.updateInstructorProfile(request.userId, profile)
          .map(lines => Ok(Json.obj("updated" -> lines)))
      }
    )
  }

  def updateTraineeProfile = traineeAction(parse.json).async { implicit request =>
    request.body.validate[TraineeProfileForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      profile => {
        userRepo.updateTraineeProfile(request.userId, profile)
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
        userRepo.updateCredentials(request.userId, credentials)
          .map(lines => Ok(Json.obj("updated" -> lines)))
      }
    )
  }

  def delete = authAction.async { implicit request =>
    userRepo.deleteUser(request.userId)
      .map(lines => Ok(Json.obj("deleted" -> lines)))
  }
}

