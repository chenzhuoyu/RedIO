package org.oxygen.redio.items

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{BlockPos, EnumFacing, StatCollector}
import net.minecraft.world.World
import org.oxygen.redio.tileentities.TileEntityProcessor

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

		case _ => false
	}

	override def onCreated(stack: ItemStack, worldIn: World, playerIn: EntityPlayer): Unit =
	{
		val nbt = new NBTTagCompound

		nbt.setString("name", "")
		nbt.setString("script", "func onSysTick() { Context.send('testio', not Context.recv('testio')) }")
		stack.setTagCompound(nbt)
	}
}
