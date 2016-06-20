package com.github.skennedy.stravadataminer

case class Slice(startIndex: Int, endIndex: Int)
case class SliceValue[T : Numeric](startIndex: Int, endIndex: Int, value: T)
case object SliceEnd
