package com.emarsys.segmentation.config
import com.typesafe.config.Config

import scala.collection.JavaConversions._

class EscherConfig(config: Config) {
  val authHeaderName: String = config.getString("auth-header-name")
  val dateHeaderName: String = config.getString("date-header-name")
  val algoPrefix: String = config.getString("algo-prefix")
  val hostName: String = config.getString("hostname")
  val port: Int = config.getInt("port")

  val credentialScope: String = config.getString("credential-scope")

  private val trustedServices = config.getConfigList("trusted-services")
  private def findTrustedService(service: String) = trustedServices.find(_.getString("name") == service)
  val services = trustedServices.map(_.getString("name")).toList

  def key(service: String): String = findTrustedService(service).map(_.getString("key")).getOrElse("")

  def secret(service: String): String = findTrustedService(service).map(_.getString("secret")).getOrElse("")
}
