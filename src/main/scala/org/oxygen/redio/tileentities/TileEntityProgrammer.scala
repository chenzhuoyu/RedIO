package org.oxygen.redio.tileentities

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import org.oxygen.redio.items.ItemMemory

class TileEntityProgrammer extends TileEntity
{
	var memory: ItemStack = null

	def updateScript(name: String, script: String) =
	{
		if (memory != null && memory.getItem == ItemMemory)
		{
			if (!memory.hasTagCompound)
				memory.setTagCompound(new NBTTagCompound)

			memory.getTagCompound.setString("name", name)
			memory.getTagCompound.setString("script", script)
		}
	}
}
