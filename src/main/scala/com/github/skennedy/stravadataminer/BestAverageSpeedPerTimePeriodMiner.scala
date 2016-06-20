package com.github.skennedy.stravadataminer

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import akka.routing.RoundRobinPool
import kiambogo.scrava.models.{Time, Velocity}

import scala.concurrent.duration._


object BestAverageSpeedPerTimePeriodMiner {

  def props(target: ActorRef) = Props(new BestAverageSpeedPerTimePeriodMiner(target))

  case class Request(duration: FiniteDuration)
  case class Response(duration: FiniteDuration, activity: Activity, averageSpeed: Float, startIndex: Int, endIndex: Int)

  case class PerActivityResponse(activityId: Int, averageSpeed: Float, startIndex: Int, endIndex: Int)
}

class BestAverageSpeedPerTimePeriodMiner(target: ActorRef) extends Actor with ActorLogging with Stash {
  import BestAverageSpeedPerTimePeriodMiner._

  override def receive: Receive = waitingForActivities

  def waitingForActivities: Receive = {
    case ActivityLoader.LoadedActivities(activities) =>
      log.debug("Got {} activities, ready to go", activities.size)
      activities.values.foreach(createBestAverageSpeedForTimePeriodActor)
      unstashAll()
      context.become(ready(activities))
    case _ =>
      stash()
  }

  def ready(activities: Map[Int, Activity]): Receive = {
    case Request(duration) =>
      log.debug("Getting best average speed for {}", duration)
      context.children.foreach(_ ! Request(duration))
      context.become(collecting(duration, activities, Seq()))
  }

  def collecting(duration: FiniteDuration, activities: Map[Int, Activity], responses: Seq[PerActivityResponse]): Receive = {
    case response: PerActivityResponse =>
      log.debug("Got response {}", response)
      val newResponses = responses :+ response
      if (newResponses.size == context.children.size) {
        target ! collectResponse(duration, activities, responses)
        unstashAll()
        context.become(ready(activities))
      } else
        context.become(collecting(duration, activities, newResponses))

    case _ =>
      stash()
  }

  def collectResponse(duration: FiniteDuration, activities: Map[Int, Activity], responses: Seq[PerActivityResponse]): Response = {
    val bestResponse = responses.reduceLeft((x, y) => if (x.averageSpeed >= y.averageSpeed) x else y)
    Response(duration, activities(bestResponse.activityId), bestResponse.averageSpeed, bestResponse.startIndex, bestResponse.endIndex)
  }

  def createBestAverageSpeedForTimePeriodActor(activity: Activity): Unit = {

    (activity.getDataStream[Velocity]("velocity_smooth"), activity.getDataStream[Time]("time")) match {
      case (Some(velocity), Some(time)) =>
        context.actorOf(Props(new BestAverageSpeedPerTimePeriodPerActivity(activity.id, velocity.data, time.data)), activity.id.toString)
      case _ =>
      // ignore
    }
  }
}

private class BestAverageSpeedPerTimePeriodPerActivity(activityId: Int, speedData: Seq[Float], timeData: Seq[Int]) extends Actor with ActorLogging with Stash {
  import BestAverageSpeedPerTimePeriodMiner._

  val averageCalculator = context.actorOf(RoundRobinPool(20).props(routeeProps = AverageCalculator.props(speedData, self)))
  val streamSlicer = context.actorOf(StreamSlicer.props(timeData, averageCalculator))

  override def receive: Receive = waitingForRequest

  def waitingForRequest: Receive = {
    case Request(duration) =>
      log.debug("Got request {}", Request(duration))
      streamSlicer ! StreamSlicer.SliceData(duration.toSeconds.toInt)
      context.become(processing(Seq(), sender()))

  }

  def processing(values: Seq[SliceValue[Float]], requester: ActorRef): Receive = {
    case v: SliceValue[Float] =>
      log.debug("Got value {}", v)
      context.become(processing(values :+ v, requester))

    case SliceEnd =>
      log.debug("End of stream")
      if (values.isEmpty)
        requester ! PerActivityResponse(activityId, 0, -1, -1)
      else {
        val bestValue = values.reduceLeft((x, y) => if (x.value >= y.value) x else y)
        requester ! PerActivityResponse(activityId, bestValue.value, bestValue.startIndex, bestValue.endIndex)
      }

      unstashAll()
      context.become(waitingForRequest)

    case _ =>
      stash()

  }
}
