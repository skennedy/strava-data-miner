package com.github.skennedy.stravadataminer

import akka.actor.{Props, Actor}
import com.github.skennedy.stravadataminer.StravaApi.{ActivitiesRequest, ActivitiesResponse, ActivityStreamRequest, ActivityStreamResponse}
import kiambogo.scrava.ScravaClient
import kiambogo.scrava.models.{PersonalActivitySummary, Streams}

object StravaApi {

  def props: Props = Props(new StravaApi)

  case class ActivitiesRequest(pageNumber: Int, pageSize: Int)
  case class ActivitiesResponse(activities: List[PersonalActivitySummary])

  case class ActivityStreamRequest(activityId: Int)
  case class ActivityStreamResponse(activityId: Int, streams: List[Streams])

}

class StravaApi extends Actor {

  val client = new ScravaClient("d20de4d0d08478776e3ed3aff0f5c33ef6f51b21")

  override def receive: Receive = {
    case ActivitiesRequest(pageNumber, pageSize) =>
      val activities = client.listAthleteActivities(page = Some(pageNumber), per_page = Some(pageSize))
      sender() ! ActivitiesResponse(activities)

    case ActivityStreamRequest(activityId) =>
      val data = client.retrieveActivityStream(activityId.toString)
      sender() ! ActivityStreamResponse(activityId, data)

  }
}
