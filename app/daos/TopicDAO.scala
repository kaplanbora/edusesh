package daos

import javax.inject.{Inject, Singleton}

import forms.UserTopicForm
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile
import models._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TopicDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._

  private class MainTopicTable(tag: Tag) extends Table[MainTopic](tag, "main_topics") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Unique)

    def * = (id, name) <> ((MainTopic.apply _).tupled, MainTopic.unapply)
  }

  private class UserTopicTable(tag: Tag) extends Table[UserTopic](tag, "user_topics") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Unique)
    def parentId = column[Long]("parent_id")

    def * = (id, name, parentId) <> ((UserTopic.apply _).tupled, UserTopic.unapply)
  }

  private val mainTopicTable = TableQuery[MainTopicTable]
  private val userTopicTable = TableQuery[UserTopicTable]

  def listMainTopics: Future[Seq[MainTopic]] = db.run {
    mainTopicTable.result
  }

  def listUserTopics: Future[Seq[UserTopic]] = db.run {
    userTopicTable.result
  }

  def createUserTopic(form: UserTopicForm): Future[Long] = db.run {
    (userTopicTable returning userTopicTable.map(_.id)) +=
      UserTopic(-1, form.name, form.parentId)
  }

  def updateUserTopic(id: Long, form: UserTopicForm): Future[Int] = db.run {
    userTopicTable.filter(_.id === id)
      .map(topic => (topic.name, topic.parentId))
      .update((form.name, form.parentId))
      .transactionally
  }

  def deleteUserTopic(id: Long): Future[Int] = db.run {
    userTopicTable.filter(_.id === id)
      .delete
      .transactionally
  }
}
