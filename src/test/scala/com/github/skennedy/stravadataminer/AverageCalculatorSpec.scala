package com.github.skennedy.stravadataminer

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{FlatSpecLike, Matchers}

class AverageCalculatorSpec extends TestKit(ActorSystem("AverageCalculatorSpec")) with FlatSpecLike with Matchers with ImplicitSender {

  val target = TestProbe()

  "An AverageCalculator" should "return the average of a whole data stream" in {
    val data: Seq[Int] = Seq(10, 11, 12, 13, 14, 15, 16)
    val calculator = constructTestActor[Int](data)

    // when
    calculator ! Slice(0, 7)

    // then
    target.expectMsg(SliceValue(0, 7, 13.0f))
  }

  it should "return the average of a slice of a data stream" in {
    val data: Seq[Int] = Seq(10, 11, 12, 13, 14, 15, 16)
    val calculator = constructTestActor[Int](data)

    // when
    calculator ! Slice(3, 6)

    // then
    target.expectMsg(SliceValue(3, 6, 14.0f))
  }

  def constructTestActor[T : Numeric](data: Seq[T]): ActorRef = {
    system.actorOf(AverageCalculator.props[T](data, target.ref))
  }
}
