package controllers

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject

import auth.Security._
import forms._
import forms.AccountForms._
import actions.AuthenticatedAction
import auth.Token
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.AccountRepository

import scala.concurrent.{ExecutionContext, Future}

class AccountController @Inject()(
    accountRepo: AccountRepository,
    authAction: AuthenticatedAction,
    cc: ControllerComponents)
  (implicit ec: ExecutionContext) extends AbstractController(cc) {

  def currentTime: Timestamp = Timestamp.valueOf(LocalDateTime.now())

  def createAccount = authAction(parse.json).async { implicit request =>
    request.body.validate[AccountForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      account => {
        accountRepo
          .create(
            account.email,
            encodePassword(account.password, account.email),
            account.firstName,
            account.lastName,
            currentTime,
            "student")
          .map(account => Created(Json.obj("account" -> Json.toJson(account))))
      }
    )
  }

  def getAccounts = authAction.async { implicit request =>
    accountRepo.list().map(a => Ok(Json.toJson(a)))
  }

  def login = Action(parse.json).async { implicit request =>
    request.body.validate[LoginForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      credentials => {
        accountRepo.byEmail(credentials.email).flatMap {
          case None => Future.successful(NotFound(Json.obj("message" -> "Account not found.")))
          case Some(account) if checkPassword(credentials.password, account) =>
            Future.successful(Ok(Json.obj("token" -> Token.generate(account))))
          case Some(_) => Future.successful(Ok(Json.obj("message" -> "No match for this email and password.")))
        }
      }
    )
  }
}
