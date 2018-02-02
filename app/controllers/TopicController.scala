package controllers

import javax.inject.Inject

import actions.{AuthenticatedAction, InstructorAction}
import daos.TopicDAO
import forms.UserTopicForm
import forms.TopicForms._
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class TopicController @Inject()(
    topicDao: TopicDAO,
    authAction: AuthenticatedAction,
    instructorAction: InstructorAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getTopics(t: String) = Action.async { implicit request =>
    t match {
      case "main" => topicDao.listMainTopics.map(topics => Ok(Json.toJson(topics)))
      case "user" => topicDao.listUserTopics.map(topics => Ok(Json.toJson(topics)))
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Unknown topic type.")))
    }
  }

  def getSelfTopics = instructorAction.async { implicit request =>
    topicDao.getTopicsForInstructor(request.userId)
      .map(topics => Ok(Json.toJson(topics)))
  }

  def getInstructorTopics(id: Long) = Action.async { implicit request =>
    topicDao.getTopicsForInstructor(id)
      .map(topics => Ok(Json.toJson(topics)))
  }

  def deleteInstructorTopic(id: Long) = instructorAction.async { implicit request =>
    topicDao.deleteInstructorTopic(request.userId, id)
      .map(lines => Ok(Json.toJson(lines > 0)))
  }

  /**
    * When an instructor adds a topic to its profile
    * First we check if that topic exists. If it exists we add it to its profile
    * If it does not exist, we create a new user topic and add it to its profile
    */
  def addTopic = instructorAction(parse.json).async { implicit request =>
    request.body.validate[UserTopicForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      topic => topicDao.getUserTopic(topic.name).flatMap {
        case Some(t) => topicDao.addInstructorTopic(request.userId, t.id)
          .map(id => Created(Json.toJson(id)))
        case None => for {
          id <- topicDao.createUserTopic(topic)
          addedId <- topicDao.addInstructorTopic(request.userId, id)
        } yield Created(Json.toJson(addedId))
      }
    )
  }
}

