package daos

import javax.inject.{Inject, Singleton}

import forms.{SelfTopic, UserTopicForm}
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

  private class InstructorTopicTable(tag: Tag) extends Table[InstructorTopic](tag, "instructor_topics") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def instructorId = column[Long]("instructor_id")
    def topicId = column[Long]("topic_id")

    def * = (id, instructorId, topicId) <> ((InstructorTopic.apply _).tupled, InstructorTopic.unapply)
  }

  private val mainTopicTable = TableQuery[MainTopicTable]
  private val userTopicTable = TableQuery[UserTopicTable]
  private val instructorTopicTable = TableQuery[InstructorTopicTable]

  def listMainTopics: Future[Seq[MainTopic]] = db.run {
    mainTopicTable.result
  }

  def listUserTopics: Future[Seq[UserTopic]] = db.run {
    userTopicTable.result
  }

  def getUserTopic(name: String): Future[Option[UserTopic]] = db.run {
    userTopicTable.filter(_.name === name)
      .result
      .headOption
  }

  def createUserTopic(form: UserTopicForm): Future[Long] = db.run {
    (userTopicTable returning userTopicTable.map(_.id)) +=
      UserTopic(-1, form.name, form.parentId)
  }

  def addInstructorTopic(instructorId: Long, topicId: Long): Future[Long] = db.run {
    (instructorTopicTable returning instructorTopicTable.map(_.id)) +=
      InstructorTopic(-1, instructorId, topicId)
  }

  def deleteInstructorTopic(instructorId: Long, topicId: Long): Future[Int] = db.run {
    instructorTopicTable
      .filter(topic => topic.topicId === topicId && topic.instructorId === instructorId)
      .delete
      .transactionally
  }

  def getTopicsForInstructor(instructorId: Long): Future[Seq[UserTopic]] = db.run {
    val topicIds = instructorTopicTable.filter(_.instructorId === instructorId).map(_.topicId)
    userTopicTable.filter(_.id in topicIds)
      .result
  }

  def getSelfTopics(instructorId: Long): Future[Seq[SelfTopic]] = db.run {
    val joinedTopics = for {
      instructorTopics <- instructorTopicTable.filter(_.instructorId === instructorId)
      topicNames <- userTopicTable.filter(_.id === instructorTopics.topicId)
    } yield instructorTopics.id -> topicNames.name
    joinedTopics.result
  }.map(result => result.map {
    case (id, name) => SelfTopic(id, name)
  })
}
