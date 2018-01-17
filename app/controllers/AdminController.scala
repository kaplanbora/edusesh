package controllers

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject

import auth.Security.encodePassword
import actions.AuthorizedAction
import forms.AccountForm
import play.api.libs.json._
import play.api.mvc._
import repositories.AccountRepository

import scala.concurrent._

class AdminController @Inject()(
    accountRepo: AccountRepository,
    adminAction: AuthorizedAction,
    cc: ControllerComponents)
  (implicit ec: ExecutionContext) extends AbstractController(cc) {
  def currentTime = Timestamp.valueOf(LocalDateTime.now())

  def getAccounts = adminAction.async { implicit request =>
    accountRepo.list()
      .map(users => Ok(Json.toJson(users)))
  }

  def register = Action(parse.json).async { implicit request =>
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
          "admin"
        ).map(account => Created(Json.obj("account" -> Json.toJson(account))))
      }
    )
  }
}
