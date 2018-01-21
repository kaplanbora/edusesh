package controllers

import javax.inject.Inject

import actions.AuthenticatedAction
import daos.TopicDAO
import forms.UserTopicForm
import forms.TopicForms._
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class TopicController @Inject()(
    topicDao: TopicDAO,
    authAction: AuthenticatedAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getMainTopics = Action.async { implicit request =>
    topicDao.listMainTopics.map(topics => Ok(Json.toJson(topics)))
  }

  def getUserTopics = Action.async { implicit request =>
    topicDao.listUserTopics.map(topics => Ok(Json.toJson(topics)))
  }

  def updateUserTopic(id: Long) = authAction(parse.json).async { implicit request =>
    request.body.validate[UserTopicForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      topic => {
        topicDao.updateUserTopic(id, topic)
          .map(lines => Ok(Json.toJson(lines)))
      }
    )
  }

  def createTopic = authAction(parse.json).async { implicit request =>
    request.body.validate[UserTopicForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      topic => {
        topicDao.createUserTopic(topic)
          .map(lines => Ok(Json.toJson(lines)))
      }
    )
  }

  def deleteTopic(id: Long) = authAction.async { implicit request =>
    topicDao.deleteUserTopic(id)
      .map(lines => Ok(Json.toJson(lines)))
  }
}

