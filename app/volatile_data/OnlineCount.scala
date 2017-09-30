package volatile_data

import models.{Account, Lesson}

import scala.collection.mutable

object OnlineCount {
  // Use atomic counter here
}

object OnlineTeachers {
  val onlineTeachers: mutable.MutableList[Account] = mutable.MutableList.empty
}

object OnlineLessons {
  val onlineLessons: mutable.MutableList[Lesson] = mutable.MutableList.empty
}
