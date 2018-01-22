package actions

import javax.inject.Inject

import models._
import util.ActionUtils._
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._
import daos.UserDAO

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedRequest[A](val credentials: UserCredentials, request: Request[A]) extends WrappedRequest[A](request)
class InstructorRequest[A](val userId: Long, request: Request[A]) extends WrappedRequest[A](request)
class TraineeRequest[A](val userId: Long, request: Request[A]) extends WrappedRequest[A](request)

// Validates the request for an existing user with any role.
class AuthenticatedAction @Inject()(bodyParser: BodyParsers.Default, userDao: UserDAO)
  (implicit ec: ExecutionContext) extends ActionBuilder[AuthenticatedRequest, AnyContent] {

  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
    val userDetails = extractTokenInfo(request)

    userDetails match {
      case Some((id, _)) => userDao.getCredentialsById(id).flatMap {
        case Some(credentials) => block(new AuthenticatedRequest(credentials, request))
        case None => Future.successful(Forbidden(Json.obj("error" -> "User not found.")))
      }
      case _ =>
        logBadRequests(request, "Authentication error.")
        Future.successful(Forbidden(Json.obj("error" -> "Authentication Error")))
    }
  }

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec
}

// Validates the request for only instructors
class InstructorAction @Inject()(bodyParser: BodyParsers.Default, userDao: UserDAO)
  (implicit ec: ExecutionContext) extends ActionBuilder[InstructorRequest, AnyContent] {

  def invokeBlock[A](request: Request[A], block: InstructorRequest[A] => Future[Result]) = {
    val userDetails = extractTokenInfo(request)

    userDetails match {
      case Some((id, role)) if role == "instructor" => userDao.getCredentialsById(id).flatMap {
        case Some(_) => block(new InstructorRequest(id, request))
        case None => Future.successful(NotFound(Json.obj("error" -> "User Not Found")))
      }
      case _ =>
        logBadRequests(request, "User not found or incorrect role.")
        Future.successful(Forbidden(Json.obj("error" -> "Authentication Error")))
    }
  }

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec
}

// Validates the request for only trainees
class TraineeAction @Inject()(bodyParser: BodyParsers.Default, userDao: UserDAO)
  (implicit ec: ExecutionContext) extends ActionBuilder[TraineeRequest, AnyContent] {

  def invokeBlock[A](request: Request[A], block: TraineeRequest[A] => Future[Result]) = {
    val userDetails = extractTokenInfo(request)

    userDetails match {
      case Some((id, role)) if role == "trainee" => userDao.getCredentialsById(id).flatMap {
        case Some(_) => block(new TraineeRequest(id, request))
        case None => Future.successful(NotFound(Json.obj("error" -> "User Not Found")))
      }
      case _ =>
        logBadRequests(request, "User not found or incorrect role.")
        Future.successful(Forbidden(Json.obj("error" -> "Authentication Error")))
    }
  }

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec
}
