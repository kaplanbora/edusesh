package daos

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models._
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChatDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] with DbMappings {

  import profile.api._

  private class ConversationTable(tag: Tag) extends Table[Conversation](tag, "conversations") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId1 = column[Long]("user1_id")
    def userId2 = column[Long]("user2_id")

    def * = (id, userId1, userId2) <> ((Conversation.apply _).tupled, Conversation.unapply)
  }

  private class MessageTable(tag: Tag) extends Table[Message](tag, "messages") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def senderId = column[Long]("sender_id")
    def receiverId = column[Long]("receiver_id")
    def conversationId = column[Long]("conversation_id")
    def body = column[String]("body")
    def date = column[LocalDateTime]("date")

    def * = (id, senderId, receiverId, conversationId, body, date) <>
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

  def getMessagesForConversation(conversationId: Long): Future[Seq[Message]] = db.run {
    messageTable.filter(_.conversationId === conversationId).result
  }

  def getConversationsForUser(userId: Long): Future[Seq[Conversation]] = db.run {
    conversationTable.filter(conv =>
      (conv.userId1 === userId) || (conv.userId2 === userId)
    ).result
  }
}
