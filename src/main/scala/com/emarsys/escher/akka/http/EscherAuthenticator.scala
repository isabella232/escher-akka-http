package com.emarsys.escher.akka.http

import java.net.InetSocketAddress

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.emarsys.escher.Escher
import com.emarsys.escher.akka.http.config.EscherConfig

import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}

trait EscherAuthenticator {

  val escherConfig: EscherConfig

  def authenticate(serviceNames: List[String], httpRequest: HttpRequest)(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {

    val escher = setupEscher(createEscherForAuth())
    val address = new InetSocketAddress(escherConfig.hostName, escherConfig.port)
    val keyPool = serviceNames.flatMap(escherConfig.keyPool).toMap

    for {
      body <- Unmarshal(httpRequest.entity).to[String]
      escherHttpRequest = new EscherHttpRequest(httpRequest.addHeader(RawHeader("Content-type", "application/json")), body)
      _ = escher.authenticate(escherHttpRequest, keyPool.asJava, address)
    } yield body
  }

  def createEscherForSigning(serviceName: String): Escher = new Escher(escherConfig.credentialScope(serviceName))

  def createEscherForAuth(): Escher = new Escher(escherConfig.credentialScope)

  def setupEscher(escher: Escher): Escher = escher
    .setAuthHeaderName(escherConfig.authHeaderName)
    .setDateHeaderName(escherConfig.dateHeaderName)
    .setAlgoPrefix(escherConfig.algoPrefix)
    .setVendorKey(escherConfig.vendorKey)

}
