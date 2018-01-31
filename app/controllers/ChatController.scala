package controllers

import java.time.LocalDateTime
import javax.inject.Inject

import actions.AuthenticatedAction
import daos.ChatDAO
import forms._
import forms.ChatForms._
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class ChatController @Inject()(
    chatDao: ChatDAO,
    authAction: AuthenticatedAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getMessages(conversationId: Long) = authAction.async { implicit request =>
    chatDao.getMessagesForConversation(conversationId)
      .map(conversations => Ok(Json.toJson(conversations)))
  }

  def createConversation(targetId: Long) = authAction.async { implicit request =>
    chatDao.getConversationForUsers(targetId, request.credentials.id).flatMap {
      case Some(conv) => Future.successful(Ok(Json.toJson(conv.id)))
      case _ => chatDao.createConversation(request.credentials.id, targetId)
        .map(id => Created(Json.toJson(id)))
    }
  }

  def createMessage(conversationId: Long) = authAction(parse.json).async { implicit request =>
    chatDao.getConversation(conversationId).flatMap {
      case Some(conv) if conv.userId1 == request.credentials.id || conv.userId2 == request.credentials.id =>
        request.body.validate[MessageForm].fold(
          errors => {
            Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
          },
          message => {
            chatDao.createMessage(conversationId, request.credentials.id, message, LocalDateTime.now())
              .map(id => Created(Json.toJson(id)))
          }
        )
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Conversation doesn't exists or invalid authentication.")))
    }
  }

  // When a user deletes a conversation its not removed but hidden from them
  def updateConversation(conversationId: Long, removed: Boolean) = authAction.async { implicit request =>
    chatDao.getConversation(conversationId).flatMap {
      case Some(conv) if removed && conv.userId1 == request.credentials.id =>
        chatDao.removeForUser1(conversationId)
          .map(lines => Ok(Json.toJson(lines)))
      case Some(conv) if removed && conv.userId2 == request.credentials.id =>
        chatDao.removeForUser2(conversationId)
          .map(lines => Ok(Json.toJson(lines)))
      case _ => Future.successful(BadRequest(Json.obj("error" -> "Conversation doesn't exists or invalid authentication.")))
    }
  }
}
