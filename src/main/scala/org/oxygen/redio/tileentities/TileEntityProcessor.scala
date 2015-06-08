package org.oxygen.redio.tileentities

import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.network.{NetworkManager, Packet}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{BlockPos, EnumParticleTypes}
import net.minecraft.world.World
import org.oxygen.redio.common.Constants

import scala.util.Random

class TileEntityProcessor extends TileEntity
{
	val random = new Random(System.currentTimeMillis())

	var heat: Long = 0
	var name: String = "test"
	var script: String = ""

	override def writeToNBT(nbt: NBTTagCompound) =
	{
		super.writeToNBT(nbt)
		nbt.setLong("heat", heat)
		nbt.setString("name", name)
		nbt.setString("script", script)
	}

	override def readFromNBT(nbt: NBTTagCompound) =
	{
		super.readFromNBT(nbt)
		heat = nbt.getLong("heat")
		name = nbt.getString("name")
		script = nbt.getString("script")
	}

	override def onDataPacket(net: NetworkManager, packet: S35PacketUpdateTileEntity) =
	{
		readFromNBT(packet.getNbtCompound)
		worldObj.setBlockState(pos, getBlockType.getStateFromMeta(packet.getTileEntityType), 2)
	}

	override def shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newSate: IBlockState) =
		oldState.getBlock != newSate.getBlock

	override def receiveClientEvent(eventId: Int, eventParam: Int): Boolean = eventId match
	{
		case Constants.Events.SMOKE =>
			val x = pos.getX + random.nextDouble()
			val z = pos.getZ + random.nextDouble()
			worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, pos.getY, z, 0.0, 0.05, 0.0)
			true

		case _ => false
	}

	override def getDescriptionPacket: Packet =
	{
		val nbt = new NBTTagCompound

		writeToNBT(nbt)
		new S35PacketUpdateTileEntity(pos, getBlockMetadata, nbt)
	}
}
