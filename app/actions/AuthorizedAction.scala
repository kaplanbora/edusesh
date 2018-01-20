package actions

import java.time.ZonedDateTime
import javax.inject.Inject

import auth._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._

class AuthorizedAction @Inject()(bodyParser: BodyParsers.Default)
  (implicit ec: ExecutionContext) extends ActionBuilderImpl(bodyParser) {
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
    implicit lazy val now = ZonedDateTime.now()

    val userRole = for {
      token <- request.headers.get("JWT")
      jwt <- Token.validate(token)
      role <- jwt.getClaim[Role]
    } yield role.value

    userRole match {
      case Some(role) if role == "admin" =>
        block(request)
      case _ =>
        Logger.info(s"[$readableDate] - Authorization error for request: ${request.body} ${request.headers}")
        Future.successful(Forbidden(Json.obj("error" -> "Authorization Error")))
    }
  }

  def readableDate(implicit date: ZonedDateTime) =
    s"${date.getYear}-${date.getMonthValue}-${date.getDayOfMonth} ${date.getHour}:${date.getMinute}"
}
