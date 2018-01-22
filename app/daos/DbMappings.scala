package daos

import java.sql.Timestamp
import java.time.LocalDateTime

import models._
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile

trait DbMappings extends HasDatabaseConfigProvider[PostgresProfile] {
  import profile.api._

  implicit val userRoleMap = MappedColumnType.base[UserRole, String](
    {
      case AdminRole => "admin"
      case InstructorRole => "instructor"
      case TraineeRole => "trainee"
    },
    {
      case "admin" => AdminRole
      case "instructor" => InstructorRole
      case "trainee" => TraineeRole
    }
  )

  implicit val timestampMap = MappedColumnType.base[LocalDateTime, Timestamp](
    ldt => Timestamp.valueOf(ldt),
    ts => ts.toLocalDateTime
  )
}
