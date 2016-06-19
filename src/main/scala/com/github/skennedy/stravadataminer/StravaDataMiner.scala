package com.github.skennedy.stravadataminer

import akka.actor.{ActorLogging, Actor, ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object StravaDataMiner extends App {
  
  val system = ActorSystem("strava-data-miner")

  val api = system.actorOf(StravaApi.props)

  val output = system.actorOf(Props(new Actor with ActorLogging {
    override def receive: Receive = {
      case ActivityLoader.LoadedActivities(activities) =>
        log.info("Got {} activities: ")
        activities.values.foreach(log.info("  {}", _))
        context.system.terminate()
    }
  }))

  val loader = system.actorOf(ActivityLoader.props(api, output))

  Await.result(system.whenTerminated, Duration.Inf)
}


