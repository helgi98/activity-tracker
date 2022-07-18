package org.helgi.activity

import io.grpc.ServerBuilder
import org.helgi.activity.auth.AuthProvider
import org.helgi.activity.config.{AppConfig, ServerConfig}
import org.helgi.activity.repo.KafkaRepo
import org.helgi.activity.service.ActivityTrackerService
import org.helgi.activity.service.activity_tracker.ZioActivityTracker.ZActivityTracker
import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import scalapb.zio_grpc.{ManagedServer, RequestContext, ServiceList}
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio._


object TrackerServer extends ZIOAppDefault {

  private def createServerBuilder(serverConfig: ServerConfig) =
    ServerBuilder.forPort(serverConfig.port)

  private val config = ZLayer.fromZIO(ZIO.attempt(ConfigSource.default.loadOrThrow[AppConfig]))

  case class ServicesProvider(activityTrackerService: ZActivityTracker[Any, RequestContext]) {
    val services: ServiceList[Any] = ServiceList.add(activityTrackerService)
  }

  private val servicesProvider = ZLayer.fromFunction(ServicesProvider.apply _)

  private val httpClient = ZLayer.fromZIO(ZIO.scoped(HttpClientZioBackend.scoped()))

  private def server =
    ZIO.scoped {
      for {
        appConfig <- ZIO.service[AppConfig]
        servicesProvider <- ZIO.service[ServicesProvider]
        server <- ManagedServer.fromServiceList(createServerBuilder(appConfig.server), servicesProvider.services)
      } yield server
    }

  override def run: ZIO[Scope, Throwable, Nothing] = server
    .provide(config,
      httpClient,
      AuthProvider.live,
      KafkaRepo.live,
      ActivityTrackerService.live,
      servicesProvider) *> ZIO.never
}
