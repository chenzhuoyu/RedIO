package org.oxygen.redio.tileentities

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import org.oxygen.redio.items.ItemMemory

class TileEntityProgrammer extends TileEntity
{
	private var inside: ItemStack = null

	def name = inside.getTagCompound.getString("name")
	def script = inside.getTagCompound.getString("script")
	def isEmpty = inside == null

	def ejectMemory() =
	{
		if (inside != null && inside.getItem == ItemMemory)
		{
			if (!worldObj.isRemote)
			{
				val dx = worldObj.rand.nextDouble() * 0.7 + 0.15
				val dy = worldObj.rand.nextDouble() * 0.7 + 0.66
				val dz = worldObj.rand.nextDouble() * 0.7 + 0.15
				val entity = new EntityItem(worldObj, pos.getX + dx, pos.getY + dy, pos.getZ + dz, inside)

				entity.setDefaultPickupDelay()
				worldObj.spawnEntityInWorld(entity)
			}

			inside = null
		}
	}

	def insertMemory(memory: ItemStack) =
	{
		if (memory.getItem == ItemMemory)
		{
			inside = memory.copy()

			if (!inside.hasTagCompound)
			{
				inside.setTagCompound(new NBTTagCompound)
				inside.getTagCompound.setString("name", "")
				inside.getTagCompound.setString("script", "")
			}

			if (!worldObj.isRemote)
			{
				markDirty()
				worldObj.markBlockForUpdate(pos)
			}
		}
	}

	def updateScript(name: String, script: String) =
	{
		if (inside != null && inside.getItem == ItemMemory)
		{
			inside.getTagCompound.setString("name", name)
			inside.getTagCompound.setString("script", script)
			ejectMemory()

			if (!worldObj.isRemote)
			{
				markDirty()
				worldObj.markBlockForUpdate(pos)
			}
		}
	}

	override def writeToNBT(nbt: NBTTagCompound) =
	{
		super.writeToNBT(nbt)

		if (inside != null)
		{
			val memory = new NBTTagCompound

			inside.writeToNBT(memory)
			nbt.setTag("inside", memory)
		}
	}

	override def readFromNBT(nbt: NBTTagCompound) =
	{
		super.readFromNBT(nbt)
		inside = if (nbt.hasKey("inside")) ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inside")) else null
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
