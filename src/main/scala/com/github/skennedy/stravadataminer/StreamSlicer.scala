package com.github.skennedy.stravadataminer

import akka.actor.{Actor, ActorRef, Props}

import scala.annotation.tailrec

object StreamSlicer {
  def props[T : Numeric](data: Seq[T], target: ActorRef): Props = Props(new StreamSlicer[T](data, target))

  case class SliceData[T : Numeric](sliceSize: T)

}

class StreamSlicer[T : Numeric](data: Seq[T], target: ActorRef) extends Actor {

  import StreamSlicer._

  override def receive: Receive = {
    case SliceData(sliceSize: T) =>
      findSlices(sliceSize, 0, 0)
  }

  @tailrec
  private def findSlices(sliceSize: T, startIndex: Int, lastEndIndex: Int): Unit = {
    val endIndex = findSliceEnd(sliceSize, startIndex, lastEndIndex)
    if (endIndex > 0) {
      target ! Slice(startIndex, endIndex)
      if (startIndex < data.length)
        findSlices(sliceSize, startIndex + 1, endIndex)
    } else {
      target ! SliceEnd
    }
  }

  def findSliceEnd(sliceSize: T, start: Int, from: Int)(implicit num: Numeric[T]): Int = {
    import num._
    def windowOverSize(dataPoint: T): Boolean = {
      dataPoint - data(start) >= sliceSize
    }
    data.indexWhere(windowOverSize, from) + 1 // so we include the last value
  }
}
