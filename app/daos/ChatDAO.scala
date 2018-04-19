package daos

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import forms.{ConversationForm, MessageForm}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models._
import slick.jdbc.PostgresProfile
import util.DbMappings

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChatDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] with DbMappings {

  import profile.api._

  private class ConversationTable(tag: Tag) extends Table[Conversation](tag, "conversations") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId1 = column[Long]("user1_id")
    def userId2 = column[Long]("user2_id")
    def userRemoved1 = column[Boolean]("user1_removed")
    def userRemoved2 = column[Boolean]("user2_removed")

    def * = (id, userId1, userId2, userRemoved1, userRemoved2) <>
      ((Conversation.apply _).tupled, Conversation.unapply)
  }

  private class MessageTable(tag: Tag) extends Table[Message](tag, "messages") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def senderId = column[Long]("sender_id")
    def sessionId = column[Long]("session_id")
    def body = column[String]("body")
    def date = column[LocalDateTime]("date")

    def * = (id, senderId, sessionId, body, date) <>
      ((Message.apply _).tupled, Message.unapply)
  }

  private val conversationTable = TableQuery[ConversationTable]
  private val messageTable = TableQuery[MessageTable]

  def listMessages: Future[Seq[Message]] = db.run {
    messageTable.result
  }

  def listConversations: Future[Seq[Conversation]] = db.run {
    conversationTable.result
  }

  def getConversationForUsers(userId1: Long, userId2: Long): Future[Option[Conversation]] = db.run {
    conversationTable.filter(conv =>
      (conv.userId1 === userId1 && conv.userId2 === userId2) ||
        (conv.userId1 === userId2 && conv.userId2 === userId1)
    ).result.headOption
  }

  def getConversation(sessionId: Long): Future[Option[Conversation]] = db.run {
    conversationTable.filter(_.id === sessionId).result.headOption
  }

  def getMessagesForConversation(sessionId: Long): Future[Seq[Message]] = db.run {
    messageTable.filter(_.sessionId === sessionId).result
  }

  def getConversationsForUser(userId: Long): Future[Seq[Conversation]] = db.run {
    conversationTable.filter(conv =>
      (conv.userId1 === userId) || (conv.userId2 === userId)
    ).result
  }

  def createConversation(userId1: Long, userId2: Long): Future[Long] = db.run {
    (conversationTable returning conversationTable.map(_.id)) +=
      Conversation(-1, userId1, userId2, false, false)
  }

  def createMessage(sessionId: Long, senderId: Long, form: MessageForm, date: LocalDateTime): Future[Long] = db.run {
    (messageTable returning messageTable.map(_.id)) +=
      Message(-1, senderId, sessionId, form.body, date)
  }

  def updateConversation(form: ConversationForm): Future[Int] = db.run {
    conversationTable.filter(conv =>
      (conv.userId1 === form.userId1) && (conv.userId2 === form.userId2)
    ).map(conv => (conv.userId1, conv.userId2, conv.userRemoved1, conv.userRemoved2))
      .update((form.userId1, form.userId2, form.userRemoved1, form.userRemoved2))
      .transactionally
  }

  def removeForUser1(sessionId: Long): Future[Int] = db.run {
    conversationTable.filter(_.id === sessionId)
      .map(_.userRemoved1)
      .update(true)
      .transactionally
  }

  def removeForUser2(sessionId: Long): Future[Int] = db.run {
    conversationTable.filter(_.id === sessionId)
      .map(_.userRemoved2)
      .update(true)
      .transactionally
  }
}
