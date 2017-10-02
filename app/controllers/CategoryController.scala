package controllers

import javax.inject._

import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.CategoryRepository

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class CategoryController @Inject()
(repo: CategoryRepository, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  case class CategoryForm(name: String)

  implicit val categoryFormFormat = Json.format[CategoryForm]


  /**
    * Get a category via its id.
    *
    * @param id Id of the category
    * @return Category object
    */
  def get(id: Long) = Action.async { implicit request =>
    repo.get(id).map {
      case Some(category) => Ok(Json.toJson(category))
      case None => NotFound(Json.obj("message" -> "Category not found."))
    }
  }

  /**
    * Create a new category.
    *
    * @return Created category object
    */
  def create = Action(parse.json).async { implicit request =>
    val result = request.body.validate[CategoryForm]
    result.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      category => {
        repo.create(category.name)
          .map(c => Created(Json.toJson(c)))
      }
    )
  }

  /**
    * List all categories.
    *
    * @return Sequence of categories
    */
  def list = Action.async { implicit request =>
    repo.list().map(x => Ok(Json.toJson(x)))
  }

  /**
    * Update a category.
    *
    * @param id Id of the category
    * @return Id of the category
    */
  def update(id: Long) = Action(parse.json).async { implicit request =>
    val result = request.body.validate[CategoryForm]
    result.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      category => {
        repo.update(id, category.name)
          .map(c => Ok(Json.toJson(c)))
      }
    )
  }
}
