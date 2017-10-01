package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import repositories.CategoryRepository

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(repo: CategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def createCategory(name: String) = Action.async { implicit request =>
    repo.create(name).map(id => Ok(Json.toJson(id)))
  }

  def listCategories = Action.async { implicit request =>
    repo.list().map(x => Ok(Json.toJson(x)))
  }
}
