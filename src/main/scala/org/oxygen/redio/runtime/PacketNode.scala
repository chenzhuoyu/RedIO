package org.oxygen.redio.runtime

import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess

import scala.collection.immutable.HashSet

trait PacketNode
{
	def isAcceptable(world: IBlockAccess, pos: BlockPos, target: String): Boolean
	def acceptPacket(world: IBlockAccess, pos: BlockPos, source: String, packet: PacketType): Any
}

object PacketNode
{
	def dispatch(world: IBlockAccess, pos: BlockPos, source: String, target: String, packet: PacketType): Option[Any] =
	{
		val path = HashSet[BlockPos]()
		dispatch(world, pos, source.trim, target.trim, packet, path)
	}

	def dispatch(world: IBlockAccess, pos: BlockPos, source: String,
		target: String, packet: PacketType, path: HashSet[BlockPos]): Option[Any] =
	{
		if (target.isEmpty)
			return None

		val newPath = path + pos
		val neighbors = Array(pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south())

		for ((neighbor, block) <- neighbors zip neighbors.map(world.getBlockState(_).getBlock)) block match
		{
			case node: PacketNode if !path.contains(neighbor) => node.isAcceptable(world, neighbor, target) match
			{
				case true  => return Some(node.acceptPacket(world, neighbor, source, packet))
				case false => dispatch(world, neighbor, source, target, packet, newPath) match
				{
					case None =>
					case Some(result) => return Some(result)
				}
			}

			case _ =>
		}

		None
	}
}
