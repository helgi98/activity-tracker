package org.helgi.activity.service

import io.grpc.Status
import org.helgi.activity.auth.AuthProvider
import org.helgi.activity.model.User
import org.helgi.activity.service.activity_tracker.{ActivityData, ActivityTrackerReply, LiveHealthData, ZioActivityTracker}
import scalapb.zio_grpc.RequestContext
import zio._

case class ActivityTrackerService() extends ZioActivityTracker.ZActivityTracker[Any, User] {
  override def liveHealthData(request: stream.Stream[Status, LiveHealthData]): ZIO[Any, Status, ActivityTrackerReply] =
    ZIO.succeed(ActivityTrackerReply("Data processed"))

  override def activityData(request: ActivityData): ZIO[Any, Status, ActivityTrackerReply] =
    ZIO.succeed(ActivityTrackerReply("Data processed"))
}

object ActivityTrackerService {
  val live: ZLayer[AuthProvider, Nothing, ZioActivityTracker.ZActivityTracker[Any, RequestContext]] = {
    ZLayer.fromZIO {
      for {
        authProvider <- ZIO.service[AuthProvider]
      } yield ActivityTrackerService().transformContextZIO(authProvider.authenticate(_).mapError(_ => Status.PERMISSION_DENIED))
    }
  }
}
