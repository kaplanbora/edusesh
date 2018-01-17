package controllers

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject

import auth.Security._
import forms._
import forms.AccountForms._
import actions.AuthenticatedAction
import auth.Token
import models.PublicAccount
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.AccountRepository

import scala.concurrent.{ExecutionContext, Future}

class AccountController @Inject()(
    accountRepo: AccountRepository,
    authAction: AuthenticatedAction,
    cc: ControllerComponents)
  (implicit ec: ExecutionContext) extends AbstractController(cc) {

  def currentTime = Timestamp.valueOf(LocalDateTime.now())

  def registerTrainee = Action(parse.json).async { implicit request =>
    request.body.validate[AccountForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      account => {
        accountRepo.create(
          account.email,
          encodePassword(account.password, account.email),
          account.firstName,
          account.lastName,
          currentTime,
          "trainee"
        ).map(account => Created(Json.obj("account" -> Json.toJson(account))))
      }
    )
  }

  def registerInstructor = Action(parse.json).async { implicit request =>
    request.body.validate[AccountForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      account => {
        accountRepo.create(
          account.email,
          encodePassword(account.password, account.email),
          account.firstName,
          account.lastName,
          currentTime,
          "instructor"
        ).map(account => Created(Json.obj("account" -> Json.toJson(account))))
      }
    )
  }

  def getOwnProfile = authAction.async { implicit request =>
    accountRepo.findById(request.userId).map {
      case Some(user) => Ok(Json.toJson(PublicAccount.from(user)))
      case None => NotFound(Json.obj("error" -> "User not found."))
    }
  }

  def getProfile(id: Long) = Action.async { implicit request =>
    accountRepo.findById(id).map {
      case Some(user) => Ok(Json.toJson(PublicAccount.from(user)))
      case None => NotFound(Json.obj("error" -> "User not found."))
    }
  }

  def updateProfile = authAction(parse.json).async { implicit request =>
    request.body.validate[ProfileForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      profile => {
        accountRepo.updateProfile(request.userId, profile.firstName, profile.lastName)
          .map(_ => Ok(Json.obj("updated" -> true)))
      }
    )
  }

  def updateCredentials = authAction(parse.json).async { implicit request =>
    request.body.validate[LoginForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      credentials => {
        accountRepo.updateCredentials(request.userId, credentials.email, encodePassword(credentials.password, credentials.email))
          .map(_ => Ok(Json.obj("updated" -> true)))
      }
    )
  }

  def login = Action(parse.json).async { implicit request =>
    request.body.validate[LoginForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      credentials => {
        accountRepo.findByEmail(credentials.email).flatMap {
          case None => Future.successful(NotFound(Json.obj("error" -> "Account not found.")))
          case Some(account) if checkPassword(credentials.password, account) =>
            Future.successful(Ok(Json.obj("token" -> Token.generate(account))))
          case Some(_) => Future.successful(Ok(Json.obj("error" -> "No match for this email and password.")))
        }
      }
    )
  }
}
