package actions

import javax.inject.Inject

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


class AuthenticatedAction @Inject()(parser: BodyParsers.Default)
  (implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    // TODO: Implement authentication check, get the account from token.
    block(request)
  }
}
