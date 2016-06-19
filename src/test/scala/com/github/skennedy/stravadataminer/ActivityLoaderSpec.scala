package com.github.skennedy.stravadataminer

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestKit, TestProbe}
import com.github.skennedy.stravadataminer.ActivityLoader.ActivityWithData
import kiambogo.scrava.models.Distance
import org.scalatest.{FlatSpecLike, Matchers}
import scala.concurrent.duration._

class ActivityLoaderSpec extends TestKit(ActorSystem("ActivityMinerSpec")) with FlatSpecLike with Matchers with StravaApiStubs {

  val api = TestProbe()
  val target = TestProbe()
  val pageSize = 2
  val activity1 = stubPersonalActivitySummary(id=1)
  val activity2 = stubPersonalActivitySummary(id=2)
  val activity3 = stubPersonalActivitySummary(id=3)
  val activityData1 = List(Distance(data=List(1, 2, 3)))
  val activityData2 = List(Distance(data=List(2, 3, 4)))
  val activityData3 = List(Distance(data=List(3, 4, 5)))

  "An ActivityLoader" should "send an ActivitiesRequest for first page on construction" in {

    // when
    val loader = constructTestActor

    // then
    api.expectMsg(StravaApi.ActivitiesRequest(1, pageSize))
  }


  it should "send an ActivitiesRequest for next page after receiving full page of activities" in {

    // before
    api.ignoreMsg{case msg: StravaApi.ActivityStreamRequest => true}
    val loader = constructTestActor
    api.expectMsgClass(classOf[StravaApi.ActivitiesRequest])

    // when
    loader ! StravaApi.ActivitiesResponse(List(activity1, activity2))

    // then
    api.expectMsg(StravaApi.ActivitiesRequest(2, pageSize))

  }

  it should "stop sending ActivitiesRequest after receiving incomplete page of activities" in {

    // before
    api.ignoreMsg{case msg: StravaApi.ActivityStreamRequest => true}
    val loader = constructTestActor
    api.expectMsgClass(classOf[StravaApi.ActivitiesRequest])

    // when
    loader ! StravaApi.ActivitiesResponse(List(stubPersonalActivitySummary(id = 1)))

    // then
    api.expectNoMsg(100 millis)

  }

  it should "send ActivityStreamRequest for all activities in full page" in {

    // before
    api.ignoreMsg{case msg: StravaApi.ActivitiesRequest => true}
    val loader = constructTestActor

    // when
    loader ! StravaApi.ActivitiesResponse(List(activity1, activity2))

    // then
    api.expectMsg(StravaApi.ActivityStreamRequest(1))
    api.expectMsg(StravaApi.ActivityStreamRequest(2))

  }

  it should "send ActivityStreamRequest for all activities in partial page" in {

    // before
    api.ignoreMsg{case msg: StravaApi.ActivitiesRequest => true}
    val loader = constructTestActor

    // when
    loader ! StravaApi.ActivitiesResponse(List(activity1))

    // then
    api.expectMsg(StravaApi.ActivityStreamRequest(1))

  }

  it should "not send loaded activities to target when awaiting pages" in {

    // before
    val loader = constructTestActor

    // when
    loader ! StravaApi.ActivitiesResponse(List(activity1, activity2))
    loader ! StravaApi.ActivityStreamResponse(activityId=1, activityData1)
    loader ! StravaApi.ActivityStreamResponse(activityId=2, activityData2)

    // then
    target.expectNoMsg(100 millis)
  }

  it should "not send loaded activities to target when awaiting data response" in {

    // before
    val loader = constructTestActor

    // when
    loader ! StravaApi.ActivitiesResponse(List(activity1, activity2))
    loader ! StravaApi.ActivityStreamResponse(activityId=1, activityData1)
    loader ! StravaApi.ActivityStreamResponse(activityId=2, activityData2)
    loader ! StravaApi.ActivitiesResponse(List(activity3))

    // then
    target.expectNoMsg(100 millis)
  }

  it should "send loaded activities to target when complete" in {

    // before
    val loader = constructTestActor

    // when
    loader ! StravaApi.ActivitiesResponse(List(activity1, activity2))
    loader ! StravaApi.ActivityStreamResponse(activityId=1, activityData1)
    loader ! StravaApi.ActivityStreamResponse(activityId=2, activityData2)
    loader ! StravaApi.ActivitiesResponse(List(activity3))
    loader ! StravaApi.ActivityStreamResponse(activityId=3, activityData3)

    // then

    target.expectMsg(ActivityLoader.LoadedActivities(Map(
      1 -> ActivityWithData(activity1, activityData1),
      2 -> ActivityWithData(activity2, activityData2),
      3 -> ActivityWithData(activity3, activityData3)
    )))

  }

  it should "send loaded activities to target when complete regardless of message order" in {

    // before
    val loader = constructTestActor

    // when
    loader ! StravaApi.ActivitiesResponse(List(activity1, activity2))
    loader ! StravaApi.ActivitiesResponse(List(activity3))
    loader ! StravaApi.ActivityStreamResponse(activityId=3, activityData3)
    loader ! StravaApi.ActivityStreamResponse(activityId=2, activityData2)
    loader ! StravaApi.ActivityStreamResponse(activityId=1, activityData1)

    // then

    target.expectMsg(ActivityLoader.LoadedActivities(Map(
      1 -> ActivityWithData(activity1, activityData1),
      2 -> ActivityWithData(activity2, activityData2),
      3 -> ActivityWithData(activity3, activityData3)
    )))

  }

  def constructTestActor: ActorRef = {
    system.actorOf(ActivityLoader.props(api.ref, target.ref, pageSize))
  }
}
