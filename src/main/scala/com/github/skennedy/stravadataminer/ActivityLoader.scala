package com.github.skennedy.stravadataminer

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import kiambogo.scrava.models.PersonalActivitySummary

object ActivityLoader {
  def props(api: ActorRef, target: ActorRef, pageSize: Int = 200): Props = Props(new ActivityLoader(api, target, pageSize))

  case class LoadedActivities(activities: Map[Int, Activity])
  
}

/**
  * Actor which handles loading all activitiy data streams from Strava.
  *
  * It does this by firstly requesting pages of activity summaries, then for each activity requesting
  * the data streams.
  */
class ActivityLoader(api: ActorRef, target: ActorRef, pageSize: Int) extends Actor with ActorLogging {

  var currentPageNumber: Option[Int] = None
  var outstandingActivities: Map[Int, PersonalActivitySummary] = Map()
  var loadedActivities: Map[Int, Activity] = Map()

  log.info("Loading activities...")
  requestNextPage()

  override def receive: Receive = {
    case StravaApi.ActivitiesResponse(activities) =>

      def requestData(activity: PersonalActivitySummary) = {
        if (activity.`type` == "Ride" && !activity.manual) {
          outstandingActivities = outstandingActivities + (activity.id -> activity)
          api ! StravaApi.ActivityStreamRequest(activity.id)
        }
      }

      activities foreach requestData

      if (activities.length == pageSize)
        requestNextPage()
      else
        currentPageNumber = None

      maybeSendResult()

    case StravaApi.ActivitiesFailure(exception) =>
      currentPageNumber = None
      maybeSendResult()

    case StravaApi.ActivityStreamResponse(activityId, data) =>

      log.debug("Got data for activity {}", activityId)

      val activity = outstandingActivities(activityId)
      loadedActivities += (activityId -> Activity(activity, data))

      outstandingActivities -= activityId

      maybeSendResult()

    case StravaApi.ActivityStreamFailure(activityId, exception) =>

      log.warning("Failed to retrieve activity {}: {}", activityId, exception.getMessage)

      outstandingActivities -= activityId

      maybeSendResult()

  }

  def maybeSendResult(): Unit = {
    if (currentPageNumber.isEmpty && outstandingActivities.isEmpty) {
      log.info("Loaded {} activities", loadedActivities.size)
      target ! ActivityLoader.LoadedActivities(loadedActivities)
    }
  }

  def requestNextPage() = {
    currentPageNumber = Some(currentPageNumber.getOrElse(0) + 1)
    api ! StravaApi.ActivitiesRequest(currentPageNumber.get, pageSize)
  }
}
