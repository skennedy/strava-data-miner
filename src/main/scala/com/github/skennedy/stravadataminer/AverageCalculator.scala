package com.github.skennedy.stravadataminer

import akka.actor.{Actor, ActorRef, Props}

object AverageCalculator {
  def props[T : Numeric](data: Seq[T], target: ActorRef) = Props(new AverageCalculator[T](data, target))

}

class AverageCalculator[T : Numeric](data: Seq[T], target: ActorRef) extends Actor {

  def calculateAverage(data: Seq[T])(implicit num: Numeric[T]): Float = {
    num.toFloat(data.sum(num)) / data.length
  }

  override def receive: Receive = {
    case Slice(startIndex, endIndex) =>
      target ! SliceValue(startIndex, endIndex, calculateAverage(data.slice(startIndex, endIndex)))

    case SliceEnd =>
      target ! SliceEnd
  }
}
