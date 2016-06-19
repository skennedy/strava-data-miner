package com.github.skennedy.stravadataminer

import kiambogo.scrava.models.{Streams, PersonalActivitySummary}

object StravaApi {

  case class ActivitiesRequest(pageNumber: Int, pageSize: Int)
  case class ActivitiesResponse(activities: List[PersonalActivitySummary])

  case class ActivityStreamRequest(activityId: Int)
  case class ActivityStreamResponse(activityId: Int, streams: List[Streams])

}
