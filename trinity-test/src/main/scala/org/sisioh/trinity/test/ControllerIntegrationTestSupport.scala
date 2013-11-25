package org.sisioh.trinity.test

import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse, Http}
import com.twitter.util.{Await => TAwait}
import java.net.{SocketAddress, InetSocketAddress}
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest, HttpMethod}
import org.sisioh.trinity.domain.io.http.{Response => IOResponse, HeaderName}
import org.sisioh.trinity.domain.mvc.http.{Request, Response}
import org.sisioh.trinity.domain.mvc.server.Server
import org.specs2.execute.{Result, AsResult}
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import scala.concurrent.duration.Duration
import scala.concurrent.{Await => SAwait, ExecutionContext}
import scala.util.Try
import org.sisioh.trinity.domain.mvc.routing.RoutingFilter
import org.sisioh.trinity.domain.mvc.{Environment, Filter}
import com.twitter.finagle.http.Request

/**
 * インテグレーションテストをサポートするためのトレイト。
 */
trait ControllerIntegrationTestSupport extends ControllerTestSupport {

  case class IntegrationTestContext(implicit val executor: ExecutionContext)
    extends TestContext


  private val httpClients = scala.collection.mutable.Map.empty[SocketAddress, Service[HttpRequest, HttpResponse]]

  private val host = "localhost"

  private val port = 7070

  protected def buildRequest
  (method: HttpMethod, path: String, content: Option[Content], headers: Map[HeaderName, String])
  (implicit testContext: TestContext): Try[Response] = {
    implicit val executor = testContext.executor
    val request = newRequest(method, path, content, headers)
    val _host = serverHost.getOrElse(host)
    val _port = serverPort.getOrElse(port)
    val address: SocketAddress = new InetSocketAddress(_host, _port)
    val client = httpClients.getOrElseUpdate(
      address,
      ClientBuilder()
        .codec(Http())
        .hosts(address)
        .hostConnectionLimit(1)
        .build()
    )
    Try {
      val finagleResponse = TAwait.result(client(request))
      val r = IOResponse(FinagleResponse(finagleResponse))
      Response(r)
    }
  }

  protected class WithServer(server: Server)(implicit executor: ExecutionContext)
    extends Around with Scope {

    private def running[T](block: => T): T = {
      synchronized {
        val future = server.start(Environment.Development).map {
          _ =>
            block
        }.flatMap {
          result =>
            server.stop().map(_ => result)
        }
        SAwait.result(future, Duration.Inf)
      }
    }

    def around[T: AsResult](t: => T): Result = {
      running(AsResult(t))
    }

  }

}
