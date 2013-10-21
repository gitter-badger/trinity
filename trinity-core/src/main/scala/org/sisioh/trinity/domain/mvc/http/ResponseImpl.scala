package org.sisioh.trinity.domain.mvc.http

import org.sisioh.trinity.domain.io.http
import org.sisioh.trinity.domain.io.http.{ResponseStatus, Version}
import org.sisioh.trinity.domain.io.infrastructure.http.AbstractResponseProxy

private[http]
class ResponseImpl(override val underlying: http.Response)
  extends AbstractResponseProxy(underlying) with Response {

  val toUnderlyingAsFinagle = underlying.toUnderlyingAsFinagle

  def this(status: ResponseStatus.Value, version: Version.Value) =
    this(http.Response(status, version))

  protected def createInstance(message: this.type): this.type =
    new ResponseImpl(message.underlying).asInstanceOf[this.type]

}