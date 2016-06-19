package com.github.skennedy.stravadataminer

import akka.actor.{Props, Actor, ActorRef}
import kiambogo.scrava.models.{Streams, PersonalActivitySummary}

object ActivityLoader {
  def props(api: ActorRef, target: ActorRef, pageSize: Int = 200): Props = Props(new ActivityLoader(api, target, pageSize))

  case class LoadedActivities(activities: Map[Int, Activity])
  
}

class ActivityLoader(api: ActorRef, target: ActorRef, pageSize: Int) extends Actor {

  var currentPageNumber: Option[Int] = None
  var outstandingActivities: Map[Int, PersonalActivitySummary] = Map()
  var loadedActivities: Map[Int, Activity] = Map()

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

      maybeSendResult()

    case StravaApi.ActivityStreamResponse(activityId, data) =>

      val activity = outstandingActivities(activityId)
      loadedActivities += (activityId -> Activity(activity, data))

      outstandingActivities -= activityId

      maybeSendResult()

  }

  def maybeSendResult(): Unit = {
    if (currentPageNumber.isEmpty && outstandingActivities.isEmpty) {
      target ! ActivityLoader.LoadedActivities(loadedActivities)
    }
  }

  def requestNextPage() = {
    currentPageNumber = Some(currentPageNumber.getOrElse(0) + 1)
    api ! StravaApi.ActivitiesRequest(currentPageNumber.get, pageSize)
  }
}
