package com.github.skennedy.stravadataminer

import kiambogo.scrava.models.{Streams, PersonalActivitySummary}


case class Activity(summary: PersonalActivitySummary, data: List[Streams]) {
  def id = summary.id
  def getDataStream[T <: Streams](streamType: String): Option[T] =
    data.find(_.`type` == streamType).map(_.asInstanceOf[T])
}
