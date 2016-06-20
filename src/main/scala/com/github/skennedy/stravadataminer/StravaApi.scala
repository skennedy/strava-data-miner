package com.github.skennedy.stravadataminer

import akka.actor.{Actor, ActorLogging, Props}
import com.github.skennedy.stravadataminer.StravaApi._
import kiambogo.scrava.ScravaClient
import kiambogo.scrava.models.{PersonalActivitySummary, Streams}

import scala.util.{Failure, Success, Try}

object StravaApi {

  def props: Props = Props(new StravaApi)

  case class ActivitiesRequest(pageNumber: Int, pageSize: Int)
  case class ActivitiesResponse(activities: List[PersonalActivitySummary])
  case class ActivitiesFailure(exception: Throwable)

  case class ActivityStreamRequest(activityId: Int)
  case class ActivityStreamResponse(activityId: Int, streams: List[Streams])
  case class ActivityStreamFailure(activityId: Int, exception: Throwable)

}

/**
  * Actor which encapsulates the Strava API.
  */
class StravaApi extends Actor with ActorLogging {

  val client = new ScravaClient("d20de4d0d08478776e3ed3aff0f5c33ef6f51b21")

  override def receive: Receive = {
    case ActivitiesRequest(pageNumber, pageSize) =>
      log.debug("Requesting activities (page {})", pageNumber)
      Try(client.listAthleteActivities(page = Some(pageNumber), per_page = Some(pageSize))) match {
        case Success(activities) =>
          sender() ! ActivitiesResponse(activities)
        case Failure(exception) =>
          sender() ! ActivitiesFailure(exception)
      }

    case ActivityStreamRequest(activityId) =>
      log.debug("Requesting activity stream ({})", activityId)
      Try(client.retrieveActivityStream(activityId.toString)) match {
        case Success(data) =>
          sender() ! ActivityStreamResponse(activityId, data)
        case Failure(exception) =>
          sender() ! ActivityStreamFailure(activityId, exception)
      }

  }
}
