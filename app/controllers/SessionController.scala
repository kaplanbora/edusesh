package controllers

import java.time.LocalDateTime
import javax.inject.Inject

import actions._
import forms._
import forms.SessionForms._
import models._
import play.api.libs.json._
import daos.SessionDAO
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedRequest[A](val credentials: UserCredentials, request: Request[A]) extends WrappedRequest[A](request)

class SessionController @Inject()(
    sessionDao: SessionDAO,
    authAction: AuthenticatedAction,
    instructorAction: InstructorAction,
    traineeAction: TraineeAction,
    cc: ControllerComponents,
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getSession(sessionId: Long) = authAction.async { implicit request =>
    sessionDao.getSession(sessionId).map {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id => Ok(Json.toJson(s))
      case _ => BadRequest(Json.obj("error" -> "Invalid request."))
    }
  }

  def updateSession(sessionId: Long) = instructorAction(parse.json).async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if s.instructorId == request.userId || s.traineeId == request.userId =>
        request.body.validate[SessionUpdateForm].fold(
          errors => {
            Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
          },
          sessionForm => sessionDao.updateSession(sessionId, sessionForm)
            .map(lines => Ok(Json.toJson(lines)))
        )
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def createSession = traineeAction(parse.json).async { implicit request =>
    request.body.validate[SessionForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      sessionForm => sessionDao.createSession(sessionForm)
        .map(id => Created(Json.toJson(id)))
    )
  }

  def getFiles(sessionId: Long) = authAction.async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id =>
        sessionDao.getFilesForSession(sessionId).map(files => Ok(Json.toJson(files)))
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def createFiles(sessionId: Long) = authAction(parse.json).async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id =>
        request.body.validate[SessionFileForm].fold(
          errors => {
            Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
          },
          fileForm => sessionDao.createSessionFile(sessionId, fileForm, LocalDateTime.now())
            .map(id => Created(Json.toJson(id)))
        )
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def getReviews(sessionId: Long) = authAction.async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if s.instructorId == request.credentials.id =>
        sessionDao.getReviewsForInstructor(request.credentials.id)
          .map(reviews => Ok(Json.toJson(reviews)))
      case Some(s) if s.traineeId == request.credentials.id =>
        sessionDao.getReviewsForTrainee(request.credentials.id)
          .map(reviews => Ok(Json.toJson(reviews)))
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def createReview(sessionId: Long) = authAction(parse.json).async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if (s.instructorId == request.credentials.id || s.traineeId == request.credentials.id) && s.isCompleted =>
        request.body.validate[ReviewForm].fold(
          errors => {
            Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
          },
          reviewForm => sessionDao.createReview(sessionId, reviewForm, LocalDateTime.now())
            .map(id => Created(Json.toJson(id)))
        )
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def updateReview(sessionId: Long, reviewId: Long) = authAction(parse.json).async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id =>
        request.body.validate[ReviewUpdateForm].fold(
          errors => {
            Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
          },
          reviewForm => sessionDao.updateReview(reviewId, reviewForm)
            .map(lines => Ok(Json.toJson(lines)))
        )
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def removeReview(sessionId: Long, reviewId: Long) = authAction(parse.json).async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id =>
        sessionDao.removeReview(reviewId).map(lines => Ok(Json.toJson(lines)))
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def getReport(sessionId: Long, reportId: Long) = authAction.async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id =>
        sessionDao.getReport(reportId).map {
          case Some(rep) if rep.userId == request.credentials.id => Ok(Json.toJson(rep))
          case _ => BadRequest(Json.obj("error" -> "Report not found or invalid authentication."))
        }
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def resolveReport(sessionId: Long, reportId: Long) = authAction.async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id =>
        sessionDao.resolveReport(reportId).map(lines => Ok(Json.toJson(lines)))
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }

  def createReport(sessionId: Long) = authAction(parse.json).async { implicit request =>
    sessionDao.getSession(sessionId).flatMap {
      case Some(s) if s.instructorId == request.credentials.id || s.traineeId == request.credentials.id =>
        request.body.validate[ReportForm].fold(
          errors => {
            Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
          },
          reportForm => sessionDao.createReport(sessionId, reportForm, LocalDateTime.now())
            .map(id => Created(Json.toJson(id)))
        )
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Invalid request.")))
    }
  }
}
