package com.github.skennedy.stravadataminer

import akka.actor.{Actor, Props}

object AverageCalculator {
  def props[T : Numeric] = Props(new AverageCalculator[T])


  case class Request[T : Numeric](data: Seq[T])

  case class Response(average: Double)

}

class AverageCalculator[T : Numeric] extends Actor {
  import AverageCalculator._

  def calculateAverage(data: Seq[T])(implicit num: Numeric[T]): Double = {
    num.toDouble(data.sum(num)) / data.length
  }

  override def receive: Receive = {
    case Request(data: Seq[T]) =>
      sender() ! Response(calculateAverage(data))
  }
}
