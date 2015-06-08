package org.oxygen.redio.runtime

import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import org.oxygen.redscript.objects.RedObject

import scala.collection.immutable.HashSet

trait PacketNode
{
	def matchTarget(world: IBlockAccess, pos: BlockPos, name: String): Boolean
	def dispatchPacket(world: IBlockAccess, pos: BlockPos, packet: RedObject): RedObject

	def routePacket(world: IBlockAccess, pos: BlockPos, name: String, packet: RedObject): Option[RedObject] =
	{
		val path = HashSet[BlockPos]()
		routePacket(world, pos, path, name, packet)
	}

	def routePacket(world: IBlockAccess, pos: BlockPos, path: HashSet[BlockPos], name: String, packet: RedObject): Option[RedObject] =
	{
		val newPath = path + pos
		val neighbors = Array(pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south())

		for ((neighbor, block) <- neighbors zip neighbors.map(world.getBlockState(_).getBlock)) block match
		{
			case node: PacketNode if !path.contains(neighbor) => node.matchTarget(world, neighbor, name) match
			{
				case true  => return Some(node.dispatchPacket(world, neighbor, packet))
				case false => node.routePacket(world, neighbor, newPath, name, packet) match
				{
					case None =>
					case Some (result) => return Some (result)
				}
			}

			case _ =>
		}

		None
	}
}
