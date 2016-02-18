package com.emarsys.escher.akka.http

import java.net.URI

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import com.emarsys.escher.EscherRequest

import scala.collection.JavaConverters._

class EscherHttpRequest(var request: HttpRequest, body: String) extends EscherRequest {

  def addHeader(fieldName: String, fieldValue: String): Unit = {
    request = request.withHeaders(request.headers ++ List(RawHeader(fieldName, fieldValue)))
  }

  def hasHeader(fieldName: String): Boolean = request.headers.exists((elem: HttpHeader) => elem.name == fieldName)

  def getRequestHeaders = request.headers.map(header => new EscherRequest.Header(header.name, header.value)).asJava

  def getBody: String = body

  def getHttpMethod: String = request.method.value

  def getURI: URI = new URI(request.getUri().toString)

  def getHttpRequest: HttpRequest = request

}
