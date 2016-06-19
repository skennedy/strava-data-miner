package com.github.skennedy.stravadataminer

import akka.actor.{Props, Actor, ActorRef}
import com.github.skennedy.stravadataminer.ActivityLoader.ActivityWithData
import kiambogo.scrava.models.{Streams, PersonalActivitySummary}

object ActivityLoader {
  def props(api: ActorRef, target: ActorRef, pageSize: Int = 200): Props = Props(new ActivityLoader(api, target, pageSize))

  case class LoadedActivities(activities: Map[Int, ActivityWithData])
  
  case class ActivityWithData(activity: PersonalActivitySummary, data: List[Streams])
}

class ActivityLoader(api: ActorRef, target: ActorRef, pageSize: Int) extends Actor {

  var currentPageNumber: Option[Int] = None
  var outstandingActivities: Map[Int, PersonalActivitySummary] = Map()
  var loadedActivities: Map[Int, ActivityWithData] = Map()

  requestNextPage()

  override def receive: Receive = {
    case StravaApi.ActivitiesResponse(activities) =>

      def requestData(activity: PersonalActivitySummary) = {
        outstandingActivities = outstandingActivities + (activity.id -> activity)
        api ! StravaApi.ActivityStreamRequest(activity.id)
      }

      activities foreach requestData

      if (activities.length == pageSize)
        requestNextPage()
      else
        currentPageNumber = None

    case StravaApi.ActivityStreamResponse(activityId, data) =>

      val activity = outstandingActivities(activityId)
      loadedActivities += (activityId -> ActivityWithData(activity, data))

      outstandingActivities -= activityId

      if (currentPageNumber.isEmpty && outstandingActivities.isEmpty) {
        target ! ActivityLoader.LoadedActivities(loadedActivities)
      }

  }

  def requestNextPage() = {
    currentPageNumber = Some(currentPageNumber.getOrElse(0) + 1)
    api ! StravaApi.ActivitiesRequest(currentPageNumber.get, pageSize)
  }
}
