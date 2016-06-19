package com.github.skennedy.stravadataminer

import akka.actor.{Stash, Actor}
import com.github.skennedy.stravadataminer.ActivityMiner.{BestAverageSpeedForTimePeriod}

import scala.concurrent.duration.FiniteDuration

object ActivityMiner {

  case class BestAverageSpeedForTimePeriod(duration: FiniteDuration)

  case class Result(activity: Activity, startIndex: Int, endIndex: Int)
}


class ActivityMiner extends Actor with Stash {


  override def receive: Receive = waitingForActivities

  def waitingForActivities: Receive = {
    case ActivityLoader.LoadedActivities(activities) =>
      context.become(ready(activities))
      unstashAll()
    case _ =>
      stash()
  }

  def ready(activities: Map[Int, Activity]): Receive = {
    case BestAverageSpeedForTimePeriod(duration) =>

  }
}
