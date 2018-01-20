package actions

import java.time.ZonedDateTime
import javax.inject.Inject

import auth.{Role, Token}
import io.igl.jwt.Sub
import models._
import models.UserCredentials.toUserRole
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

class UserRequest[A](val userId: Long, val userRole: UserRole, request: Request[A]) extends WrappedRequest[A](request)

class AuthenticatedAction @Inject()(bodyParser: BodyParsers.Default)
  (implicit ec: ExecutionContext) extends ActionBuilder[AuthenticatedRequest, AnyContent] {
  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
    implicit lazy val now = ZonedDateTime.now()

    val userDetails = for {
      token <- request.headers.get("JWT")
      jwt <- Token.validate(token)
      sub <- jwt.getClaim[Sub]
      role <- jwt.getClaim[Role]
    } yield (sub.value.toLong, role.value)


    userDetails match {
      case Some((id, role)) =>
        block(new AuthenticatedRequest(id, toUserRole(role), request))
      case _ =>
        Logger.info(s"[$readableDate] - Authentication error for request: ${request.body} ${request.headers}")
        Future.successful(Forbidden(Json.obj("error" -> "Authentication Error")))
    }
  }

  def readableDate(implicit date: ZonedDateTime) =
    s"${date.getYear}-${date.getMonthValue}-${date.getDayOfMonth} ${date.getHour}:${date.getMinute}"

  override def parser: BodyParser[AnyContent] = bodyParser

  override protected def executionContext: ExecutionContext = ec
}
