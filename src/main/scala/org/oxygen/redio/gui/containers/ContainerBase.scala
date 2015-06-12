package org.oxygen.redio.gui.containers

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity

abstract class ContainerBase(val tileEntity: TileEntity) extends Container
{
	override def canInteractWith(playerIn: EntityPlayer): Boolean = true
}
