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

class AuthenticatedAction @Inject()(parser: BodyParsers.Default)
  (implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    val userId = for {
      token <- request.headers.get("JWT")
      jwt <- Token.validate(token)
      sub <- jwt.getClaim[Sub]
    } yield sub.value.toLong

    userId match {
      case Some(id) =>
        Logger.info(s"[${readableDate(ZonedDateTime.now())}] - Successful authentication for user: $id")
        block(request)
      case _ =>
        Logger.info(s"[${readableDate(ZonedDateTime.now())}] - Authentication error for request: ${request.body} ${request.headers}")
        Future.successful(Forbidden(Json.obj("error" -> "Authentication Error")))
    }
  }

  def readableDate(date: ZonedDateTime) =
    s"${date.getYear}-${date.getMonthValue}-${date.getDayOfMonth} ${date.getHour}:${date.getMinute}"
}
