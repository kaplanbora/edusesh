package controllers

import java.time.LocalDateTime
import javax.inject.Inject

import actions._
import forms._
import models._
import play.api.libs.json._
import daos.SessionDAO
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedRequest[A](val credentials: UserCredentials, request: Request[A]) extends WrappedRequest[A](request)

class SessionController @Inject()(
    sessionDao: SessionDAO,
    sessionAction: SessionAction,
    authAction: AuthenticatedAction,
    cc: ControllerComponents,
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getSession(id: Long) = authAction.async { implicit request =>
    sessionDao.getSession(id).map {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id => Ok(Json.toJson(s))
      case _ => BadRequest(Json.obj("error" -> "Invalid request."))
    }
  }

  def updateSession(id: Long) = authAction(parse.json).async { implicit request =>
    sessionDao.getSession(id).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id => {
        request.body.validate[SessionUpdateForm].fold(
          errors => {
            Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
          },
          sessionForm => sessionDao.updateSession(id, sessionForm)
            .map(lines => Ok(Json.toJson(lines)))
        )
      }
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def createSession = authAction(parse.json).async { implicit request =>
    request.body.validate[SessionForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      sessionForm => sessionDao.createSession(sessionForm)
        .map(id => Created(Json.toJson(id)))
    )
  }

  def getFiles(id: Long) = authAction.async { implicit request =>
    sessionDao.getSession(id).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id => {
        sessionDao.getFilesForSession(id).map(files => Ok(Json.toJson(files)))
      }
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def createFiles(id: Long) = authAction(parse.json).async { implicit request =>
    sessionDao.getSession(id).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id => {
        request.body.validate[SessionFileForm].fold(
          errors => {
            Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
          },
          fileForm => sessionDao.createSessionFile(fileForm, LocalDateTime.now())
            .map(id => Created(Json.toJson(id)))
        )
      }
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

}
