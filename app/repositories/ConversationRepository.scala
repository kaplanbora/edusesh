package repositories

import javax.inject.{Inject, Singleton}

import models.{Account, Conversation}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConversationRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class ConversationTable(tag: Tag) extends Table[Conversation](tag, "Conversation") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def account1Id = column[Long]("account1_id")

    def account2Id = column[Long]("account2_id")

    def * = (id, account1Id, account2Id).mapTo[Conversation]

    def account1 = foreignKey("conversationAccountFK1", account1Id, TableQuery[Account])(_.id)

    def account2 = foreignKey("conversationAccountFK2", account2Id, TableQuery[Account])(_.id)
  }

  private val conversations = TableQuery[ConversationTable]

  def list(): Future[Seq[Conversation]] = db.run {
    conversations.result
  }

  def get(id: Long): Future[Option[Conversation]] = db.run {
    conversations
      .filter(_.id === id)
      .result
      .headOption
  }

  def getByAccount(accountId: Long): Future[Seq[Conversation]] = db.run {
    conversations
      .filter(c => c.account1Id === accountId || c.account2Id === accountId)
      .result
  }

  def create(acc1Id: Long, acc2Id: Long): Future[Conversation] = db.run {
    (conversations.map(c => (c.account1Id, c.account2Id))
      returning conversations.map(_.id)
      into ((values, id) => Conversation(id, values._1, values._2))
      ) += (acc1Id, acc2Id)
  }

  def delete(id: Long): Future[Int] = db.run {
    conversations
      .filter(_.id === id)
      .delete
  }
}
