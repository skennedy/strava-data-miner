package com.github.skennedy.stravadataminer

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.RoundRobinPool
import kiambogo.scrava.models.Time

import scala.concurrent.Await
import scala.concurrent.duration._

object StravaDataMiner extends App {
  val system = ActorSystem("strava-data-miner")

  val api = system.actorOf(RoundRobinPool(20).props(routeeProps = StravaApi.props))

  val output = system.actorOf(Props(new Actor with ActorLogging {

    def formatPeriod(s: Integer):String = "%d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
    def mph(metresPerSecond: Float): Float = metresPerSecond * 2.23694f

    override def receive: Receive = {
      case BestAverageSpeedPerTimePeriodMiner.Response(duration, activity, averageSpeed, startIndex, endIndex) =>
        val timeData = activity.getDataStream[Time]("time").get.data
        val startTime = formatPeriod(timeData(startIndex))
        val endTime = formatPeriod(timeData(endIndex))
        log.info("Best {} average speed: {}mph: {}", duration, mph(averageSpeed), s"https://www.strava.com/activities/${activity.id} from $startTime to $endTime")
    }
  }))

  val miner = system.actorOf(BestAverageSpeedPerTimePeriodMiner.props(output))

  val loader = system.actorOf(ActivityLoader.props(api, miner))

  miner ! BestAverageSpeedPerTimePeriodMiner.Request(10 seconds)
  miner ! BestAverageSpeedPerTimePeriodMiner.Request(30 seconds)
  miner ! BestAverageSpeedPerTimePeriodMiner.Request(1 minute)
  miner ! BestAverageSpeedPerTimePeriodMiner.Request(5 minute)
  miner ! BestAverageSpeedPerTimePeriodMiner.Request(10 minute)
  miner ! BestAverageSpeedPerTimePeriodMiner.Request(30 minute)
  miner ! BestAverageSpeedPerTimePeriodMiner.Request(1 hour)
  miner ! BestAverageSpeedPerTimePeriodMiner.Request(2 hours)

  Await.result(system.whenTerminated, Duration.Inf)
}


