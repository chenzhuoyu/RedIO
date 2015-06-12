package org.oxygen.redio.runtime

import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess

import scala.collection.immutable.HashSet

trait PacketNode
{
	def isAcceptable(world: IBlockAccess, pos: BlockPos, name: String): Boolean
	def acceptPacket(world: IBlockAccess, pos: BlockPos, packet: PacketType): Any
}

object PacketNode
{
	def dispatch(world: IBlockAccess, pos: BlockPos, name: String, packet: PacketType): Option[Any] =
	{
		val path = HashSet[BlockPos]()
		dispatch(world, pos, path, name.trim, packet)
	}

	def dispatch(world: IBlockAccess, pos: BlockPos, path: HashSet[BlockPos], name: String, packet: PacketType): Option[Any] =
	{
		if (name.isEmpty)
			return None

		val newPath = path + pos
		val neighbors = Array(pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south())

		for ((neighbor, block) <- neighbors zip neighbors.map(world.getBlockState(_).getBlock)) block match
		{
			case node: PacketNode if !path.contains(neighbor) => node.isAcceptable(world, neighbor, name) match
			{
				case true  => return Some(node.acceptPacket(world, neighbor, packet))
				case false => dispatch(world, neighbor, newPath, name, packet) match
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
