package org.oxygen.redio.items

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{BlockPos, ChatComponentTranslation, EnumFacing, StatCollector}
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler
import org.oxygen.redio.common.Constants
import org.oxygen.redio.tileentities.{TileEntityProcessor, TileEntityProgrammer}

object ItemMemory extends Item
{
	setMaxStackSize(1)
	setUnlocalizedName("memory")

	override def getShareTag: Boolean = true
	override def getMaxDamage: Int = 0
	override def getItemStackDisplayName(stack: ItemStack): String =
	{
		val name = this.getUnlocalizedNameInefficiently(stack)
		val display = if (stack.hasTagCompound) stack.getTagCompound.getString("name") else ""
		StatCollector.translateToLocal(name + ".name").trim + (if (display.isEmpty) "" else ": " + display)
	}

	override def onItemUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World,
	   pos: BlockPos, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = worldIn.getTileEntity(pos) match
	{
		case te: TileEntityProcessor =>
			te.insertMemory(stack)
			stack.stackSize -= 1
			true

		case te: TileEntityProgrammer =>
			if (!te.isEmpty)
			{
				if (worldIn.isRemote)
					playerIn.addChatMessage(new ChatComponentTranslation("chat.programmer.occupied"))

				return true
			}

			te.insertMemory(stack)
			stack.stackSize -= 1

			if (worldIn.isRemote)
				FMLNetworkHandler.openGui(playerIn, Constants.MOD_ID,
					Constants.Gui.EditSource.ID, worldIn, pos.getX, pos.getY, pos.getZ)

			true

		case _ => false
	}

	override def onCreated(stack: ItemStack, worldIn: World, playerIn: EntityPlayer): Unit =
	{
		val nbt = new NBTTagCompound

		nbt.setString("name", "")
		nbt.setString("script", "")
		stack.setTagCompound(nbt)
	}
}
