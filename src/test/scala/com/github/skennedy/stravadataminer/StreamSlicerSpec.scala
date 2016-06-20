package com.github.skennedy.stravadataminer

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import kiambogo.scrava.models.Distance
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

class StreamSlicerSpec extends TestKit(ActorSystem("StreamSlicerSpec")) with FlatSpecLike with Matchers with ImplicitSender {

  val target = TestProbe()

  "An Integer StreamSlicer" should "slice a data stream into pieces of correct size" in {

    // when
    val data: Seq[Int] = Seq(10, 11, 12, 13, 14, 15, 16)
    val slicer = constructTestActor[Int](data)
    val sliceSize: Int = 2
    slicer ! StreamSlicer.SliceData(sliceSize)

    // then
    target.expectMsg(Slice(0, 3))
    target.expectMsg(Slice(1, 4))
    target.expectMsg(Slice(2, 5))
    target.expectMsg(Slice(3, 6))
    target.expectMsg(Slice(4, 7))
  }

  "An Float StreamSlicer" should "slice a data stream into pieces of correct size" in {

    // when
    val data: Seq[Float] = Seq(0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f)
    val slicer = constructTestActor[Float](data)

    val sliceSize: Float = 0.49f
    slicer ! StreamSlicer.SliceData(sliceSize)

    target.expectMsg(Slice(0, 6))
    target.expectMsg(Slice(1, 7))
    target.expectMsg(Slice(2, 8))
    target.expectMsg(Slice(3, 9))
    target.expectMsg(Slice(4, 10))
    target.expectMsg(Slice(5, 11))

  }

  def constructTestActor[T : Numeric](data: Seq[T]): ActorRef = {
    system.actorOf(StreamSlicer.props[T](data, target.ref))
  }
}
