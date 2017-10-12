package repositories

import java.sql.Timestamp
import javax.inject._

import models.Message
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent._

@Singleton
class MessageRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._

  private class MessageTable(tag: Tag) extends Table[Message](tag, "Message") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def senderId = column[Long]("sender_id")

    def receiverId = column[Long]("receiver_id")

    def conversationId = column[Long]("conversation_id")

    def body = column[String]("body")

    def date = column[Timestamp]("date")

    def * = (id, senderId, receiverId, conversationId, body, date) <> ((Message.apply _).tupled, Message.unapply)

  }

  private val messages = TableQuery[MessageTable]

  private val messageById = Compiled((id: Rep[Long]) => messages.filter(_.id === id))

  def list(): Future[Seq[Message]] = db.run {
    messages.result
  }

  def get(id: Long): Future[Seq[Message]] = db.run {
    messageById(id).result
  }

  def getByConversation(convId: Long): Future[Seq[Message]] = db.run {
    messages
      .filter(_.conversationId === convId)
      .result
  }

  def getBySender(convId: Long): Future[Seq[Message]] = db.run {
    messages
      .filter(_.conversationId === convId)
      .result
  }

  def create(message: Message): Future[Int] = db.run {
    messages += message
  }

  def update(id: Long, message: Message): Future[Int] = db.run {
    messageById(id).update(message).transactionally
  }

  def delete(id: Long): Future[Int] = db.run {
    messageById(id).delete
  }

}
