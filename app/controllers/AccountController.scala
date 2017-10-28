package controllers

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject

import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.AccountRepository

import scala.concurrent.{ExecutionContext, Future}

class AccountController @Inject()(accountRepo: AccountRepository, cc: ControllerComponents)
                                 (implicit ec: ExecutionContext) extends AbstractController(cc) {

  case class AccountForm(email: String, password: String, firstName: String, lastName: String)

  implicit val accountFormFormat = Json.format[AccountForm]

  def currentTime: Timestamp = Timestamp.valueOf(LocalDateTime.now())

  def createAccount = Action(parse.json).async { implicit request =>
    val result = request.body.validate[AccountForm]
    result.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      account => {
        accountRepo
          .create(account.email, account.password, account.firstName, account.lastName, currentTime, "student")
          .map(id => Created(Json.toJson(id)))
      }
    )
  }

  def getAccounts = Action.async { implicit request =>
    accountRepo.list().map(a => Ok(Json.toJson(a)))
  }
}
