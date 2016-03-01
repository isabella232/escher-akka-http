package com.emarsys.escher.akka.http

import java.net.InetSocketAddress

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.emarsys.escher.Escher
import com.emarsys.escher.akka.http.config.EscherConfig

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

trait EscherAuthenticator {

  val escherConfig: EscherConfig

  def authenticate(serviceNames: List[String], httpRequest: HttpRequest)(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {

    val escher = setupEscher(createEscherForAuth())
    val address = new InetSocketAddress(escherConfig.hostName, escherConfig.port)
    val keymap = serviceNames.map(serviceName => escherConfig.key(serviceName) -> escherConfig.secret(serviceName)).toMap

    for {
      body <- Unmarshal(httpRequest.entity).to[String]
    } yield {
      val escherHttpRequest = new EscherHttpRequest(httpRequest.addHeader(RawHeader("Content-type", "application/json")), body)
      escher.authenticate(
        escherHttpRequest,
        keymap.asJava,
        address
      )
      body
    }
  }

  def createEscherForSigning(serviceName: String): Escher = new Escher(escherConfig.credentialScope(serviceName))

  def createEscherForAuth(): Escher = new Escher(escherConfig.credentialScope)

  def setupEscher(escher: Escher) = escher
    .setAuthHeaderName(escherConfig.authHeaderName)
    .setDateHeaderName(escherConfig.dateHeaderName)
    .setAlgoPrefix(escherConfig.algoPrefix)

}
