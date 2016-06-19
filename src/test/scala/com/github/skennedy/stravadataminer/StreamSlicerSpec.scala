package com.github.skennedy.stravadataminer

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import kiambogo.scrava.models.Distance
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

class StreamSlicerSpec extends TestKit(ActorSystem("StreamSlicerSpec")) with FlatSpecLike with Matchers with ImplicitSender {
  
  "An Integer StreamSlicer" should "slice a data stream into pieces of correct size" in {

    // when
    val slicer = constructTestActor[Int]()
    val data: Seq[Int] = Seq(10, 11, 12, 13, 14, 15, 16)
    val sliceSize: Int = 2
    slicer ! StreamSlicer.SliceData(data, sliceSize)

    // then
    expectMsg(StreamSlicer.Slice(data, 0, 2))
    expectMsg(StreamSlicer.Slice(data, 1, 3))
    expectMsg(StreamSlicer.Slice(data, 2, 4))
    expectMsg(StreamSlicer.Slice(data, 3, 5))
    expectMsg(StreamSlicer.Slice(data, 4, 6))
  }

  "An Float StreamSlicer" should "slice a data stream into pieces of correct size" in {

    // when
    val slicer = constructTestActor[Float]()

    val data: Seq[Float] = Seq(0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f)
    val sliceSize: Float = 0.49f
    slicer ! StreamSlicer.SliceData(data, sliceSize)

    expectMsg(StreamSlicer.Slice(data, 0, 5))
    expectMsg(StreamSlicer.Slice(data, 1, 6))
    expectMsg(StreamSlicer.Slice(data, 2, 7))
    expectMsg(StreamSlicer.Slice(data, 3, 8))
    expectMsg(StreamSlicer.Slice(data, 4, 9))
    expectMsg(StreamSlicer.Slice(data, 5, 10))

  }

  def constructTestActor[T : Numeric](): ActorRef = {
    system.actorOf(StreamSlicer.props[T])
  }
}
