package actions

import java.time.ZonedDateTime
import javax.inject.Inject

import auth.Token
import io.igl.jwt.Sub
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

class UserRequest[A](val userId: Long, request: Request[A]) extends WrappedRequest[A](request)

class AuthenticatedAction @Inject()(bodyParser: BodyParsers.Default)
  (implicit ec: ExecutionContext) extends ActionBuilder[UserRequest, AnyContent] {
  def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]) = {
    implicit lazy val now = ZonedDateTime.now()

    val userId = for {
      token <- request.headers.get("JWT")
      jwt <- Token.validate(token)
      sub <- jwt.getClaim[Sub]
    } yield sub.value.toLong

    userId match {
      case Some(id) =>
        block(new UserRequest(id, request))
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
