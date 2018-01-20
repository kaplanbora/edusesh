package util

import java.time.LocalDateTime

import auth.{Role, Token}
import io.igl.jwt.Sub
import play.api.Logger
import play.api.mvc.Request

object ActionUtils {
  def readableDate(date: LocalDateTime) =
    s"${date.getYear}-${date.getMonthValue}-${date.getDayOfMonth} ${date.getHour}:${date.getMinute}"

  def logBadRequests[A](request: Request[A], message: String) = {
    val date = readableDate(LocalDateTime.now())
    Logger.info(s"[$date] - $message: ${request.headers} ${request.body}")
  }

  def extractTokenInfo[A](request: Request[A]): Option[(Long, String)] =
    for {
      token <- request.headers.get("JWT")
      jwt <- Token.validate(token)
      sub <- jwt.getClaim[Sub]
      role <- jwt.getClaim[Role]
    } yield (sub.value.toLong, role.value)
}
