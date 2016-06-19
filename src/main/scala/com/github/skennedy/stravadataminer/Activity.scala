package com.github.skennedy.stravadataminer

import kiambogo.scrava.models.{Streams, PersonalActivitySummary}


case class Activity(summary: PersonalActivitySummary, data: List[Streams]) {
  def id = summary.id
  def getDataStream[T <: Streams]: Option[T] = data.find{
    case msg: T => true
    case _ => false
  }.map(_.asInstanceOf[T])
}
