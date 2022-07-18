package org.helgi.activity.service

import io.grpc.Status
import org.helgi.activity.auth.AuthProvider
import org.helgi.activity.model.User
import org.helgi.activity.repo.KafkaRepo
import org.helgi.activity.service.activity_tracker.{ActivityTrackerReply, AggregatedActivityData, LiveActivityData, ZioActivityTracker}
import scalapb.zio_grpc.RequestContext
import zio._


case class ActivityTrackerService(kafkaIntegration: KafkaRepo) extends ZioActivityTracker.ZActivityTracker[Any, User] {
  override def sendLiveData(liveData: stream.Stream[Status, LiveActivityData]): ZIO[User, Status, ActivityTrackerReply] =
    for {
      user <- ZIO.service[User]
      _ <- kafkaIntegration.streamLiveData(user, liveData)
    } yield ActivityTrackerReply.of("Data processed")


  override def sendAggregatedData(aggregatedData: AggregatedActivityData): ZIO[User, Status, ActivityTrackerReply] =
    for {
      user <- ZIO.service[User]
      _ <- kafkaIntegration.sendAggregatedData(user, aggregatedData)
    } yield ActivityTrackerReply.of("Data processed")
}

object ActivityTrackerService {
  val live = ZLayer.fromZIO {
    for {
      authProvider <- ZIO.service[AuthProvider]
      kafkaRepo <- ZIO.service[KafkaRepo]
      service = ActivityTrackerService(kafkaRepo)
    } yield service.transformContextZIO((rc: RequestContext) => authProvider.authenticate(rc).mapError(_ => Status.PERMISSION_DENIED))
  }
}
