package org.oxygen.redio.runtime

trait PacketType
case object Input extends PacketType
case class Output(payload: Any) extends PacketType
