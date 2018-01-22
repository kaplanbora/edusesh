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

// TODD: Add messageAction to check if a user has access to conversation its trying to reach
class ChatController @Inject()(
    chatDao: ChatDAO,
    authAction: AuthenticatedAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // If a user deleted this conversation don't send the conversation to them
  def getConversationsForUser = authAction.async { implicit request =>
    chatDao.getConversationsForUser(request.credentials.id).map(conversations => {
      conversations.filter(conv => (conv.userId1, conv.userId2) match {
        case (request.credentials.id, _) if conv.userRemoved1 => false
        case (_, request.credentials.id) if conv.userRemoved2 => false
        case _ => true
      })
      Ok(Json.toJson(conversations))
    })
  }

  def getMessages(conversationId: Long) = authAction.async { implicit request =>
    chatDao.getMessagesForConversation(conversationId)
      .map(conversations => Ok(Json.toJson(conversations)))
  }

  def createConversation = authAction(parse.json).async { implicit request =>
    request.body.validate[ConversationForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      conversation => {
        chatDao.createConversation(conversation)
          .map(id => Created(Json.toJson(id)))
      }
    )
  }

  def createMessage(id: Long) = authAction(parse.json).async { implicit request =>
    request.body.validate[MessageForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      message => {
        chatDao.createMessage(id, message, LocalDateTime.now())
          .map(id => Created(Json.toJson(id)))
      }
    )
  }

  // When a user deletes a conversation its not removed but hidden from them
  def updateConversation = authAction(parse.json).async {implicit request =>
    request.body.validate[ConversationForm].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      conversation => {
        chatDao.updateConversation(conversation)
          .map(lines => Ok(Json.toJson(lines)))
      }
    )
  }
}
