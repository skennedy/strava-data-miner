package com.github.skennedy.stravadataminer

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{FlatSpecLike, Matchers}

class AverageCalculatorSpec extends TestKit(ActorSystem("AverageCalculatorSpec")) with FlatSpecLike with Matchers with ImplicitSender {
  
  "A AverageCalculator" should "return the average of a data stream" in {
    val calculator = constructTestActor[Int]()

    // when
    val data: Seq[Int] = Seq(10, 11, 12, 13, 14, 15, 16)
    calculator ! AverageCalculator.Request(data)

    // then
    expectMsg(AverageCalculator.Response(13.0))
  }

  def constructTestActor[T : Numeric](): ActorRef = {
    system.actorOf(AverageCalculator.props[T])
  }
}
