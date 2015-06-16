package org.oxygen.redio.runtime

import org.oxygen.redio.tileentities.TileEntityProcessor

class Context(private val processor: TileEntityProcessor)
{
	def recv(source: String): Any =
	{
		PacketNode.dispatch(processor.getWorld, processor.getPos, source, Input) match
		{
			case None => null
			case Some(result) => result
		}
	}

	def send(target: String, payload: Any): Any =
	{
		PacketNode.dispatch(processor.getWorld, processor.getPos, target, Output(payload)) match
		{
			case None => null
			case Some(result) => result
		}
	}
}
