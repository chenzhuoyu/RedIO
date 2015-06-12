package org.oxygen.redio.tileentities

import java.lang

import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.network.{NetworkManager, Packet}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import org.oxygen.redio.blocks.BlockPort
import org.oxygen.redio.common.Utils
import org.oxygen.redio.runtime.{Input, Output, PacketType}

class TileEntityPort extends TileEntity
{
	var name: String = ""

	def dispatchPacket(packet: PacketType): Any = packet match
	{
		case Input => Utils.getRedStonePower(worldObj, pos) > 0
		case Output(payload) => payload match
		{
			case x: Boolean => worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockPort.POWERING, x));
			case _ => null
		}
	}

	override def writeToNBT(nbt: NBTTagCompound) =
	{
		super.writeToNBT(nbt)
		nbt.setString("name", name)
	}

	override def readFromNBT(nbt: NBTTagCompound) =
	{
		super.readFromNBT(nbt)
		name = nbt.getString("name")
	}

	override def onDataPacket(net: NetworkManager, packet: S35PacketUpdateTileEntity) =
	{
		readFromNBT(packet.getNbtCompound)
		worldObj.setBlockState(pos, getBlockType.getStateFromMeta(packet.getTileEntityType), 2)
	}

	override def shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newSate: IBlockState) =
		oldState.getBlock != newSate.getBlock

	override def getDescriptionPacket: Packet =
	{
		val nbt = new NBTTagCompound

		writeToNBT(nbt)
		new S35PacketUpdateTileEntity(pos, getBlockMetadata, nbt)
	}
}
