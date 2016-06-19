package com.github.skennedy.stravadataminer

import akka.actor.{Actor, Props}

object StreamSlicer {
  def props[T : Numeric]: Props = Props(new StreamSlicer[T])

  case class SliceData[T : Numeric](data: Seq[T], sliceSize: T)

  case class Slice[T : Numeric](data: Seq[T], startIndex: Int, endIndex: Int)

}

class StreamSlicer[T : Numeric] extends Actor {

  import StreamSlicer._

  override def receive: Receive = {
    case SliceData(data: Seq[T], sliceSize: T) =>
      findSlices(data, sliceSize, 0, 0)
  }

  def findSlices(data: Seq[T], sliceSize: T, startIndex: Int, lastEndIndex: Int): Unit = {
    val endIndex = findSliceEnd(data, sliceSize, startIndex, lastEndIndex)
    if (endIndex != -1) {
      sender() ! Slice(data, startIndex, endIndex)
      if (startIndex < data.length)
        findSlices(data, sliceSize, startIndex + 1, endIndex)
    }
  }

  def findSliceEnd(data: Seq[T], sliceSize: T, start: Int, from: Int)(implicit num: Numeric[T]): Int = {
    import num._
    def windowOverSize(dataPoint: T): Boolean = {
      dataPoint - data(start) >= sliceSize
    }
    data.indexWhere(windowOverSize, from)
  }
}
