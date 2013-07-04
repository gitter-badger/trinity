package org.sisioh.trinity.example

import org.sisioh.trinity.domain.controller.GlobalSettings
import org.sisioh.trinity.domain.http.{TrinityResponseBuilder, TrinityRequest}
import com.twitter.util.{FuturePool, Future}
import com.twitter.finagle.http.Response
import org.sisioh.trinity.domain.config.Config
import org.sisioh.trinity.application.TrinityApplication
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import org.sisioh.trinity.view.scalate.ScalateEngineContext
import org.sisioh.trinity.view.thymeleaf.ThymeleafEngineContext
import org.sisioh.trinity.view.velocity.VelocityEngineContext
import org.sisioh.trinity.view.freemarker.FreeMarkerEngineContext
import org.jboss.netty.handler.codec.http.HttpResponseStatus

class UnauthorizedException extends Exception

trait ApplicationContext {

  val globalSettings = new GlobalSettings {
    def error(request: TrinityRequest): Future[Response] = {
      request.error match {
        case Some(e: ArithmeticException) =>
          TrinityResponseBuilder().withStatus(HttpResponseStatus.valueOf(500)).withPlain("whoops, divide by zero!").toFinagleResponseFuture
        case Some(e: UnauthorizedException) =>
          TrinityResponseBuilder().withStatus(HttpResponseStatus.valueOf(401)).withPlain("Not Authorized!").toFinagleResponseFuture
        case Some(e) =>
          TrinityResponseBuilder().withStatus(HttpResponseStatus.valueOf(415)).withPlain("Unsupported Media Type!").toFinagleResponseFuture
        case _ =>
          TrinityResponseBuilder().withStatus(HttpResponseStatus.valueOf(500)).withPlain("Something went wrong!").toFinagleResponseFuture
      }
    }

    def notFound(request: TrinityRequest): Future[Response] = {
      TrinityResponseBuilder().withStatus(HttpResponseStatus.valueOf(404)).withPlain("not found yo").toFinagleResponseFuture
    }

  }

  implicit val config = Config()
  implicit val application = TrinityApplication(config, Some(globalSettings))

  // Thread Pool
  val threadPool = Executors.newFixedThreadPool(10)
  implicit val futurePool = FuturePool(threadPool)
  implicit val executor = ExecutionContext.fromExecutor(threadPool)

  implicit val scalate = ScalateEngineContext()
  implicit val thymeleaf = ThymeleafEngineContext()
  implicit val velocity = VelocityEngineContext()
  implicit val freemarker = FreeMarkerEngineContext(getClass)
}