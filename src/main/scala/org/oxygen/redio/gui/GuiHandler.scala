package org.oxygen.redio.gui

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.oxygen.redio.common.Constants
import org.oxygen.redio.gui.containers.{ContainerEditSource, ContainerSetName}

object GuiHandler extends IGuiHandler
{
	@SideOnly(Side.CLIENT)
	override def getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = id match
	{
		case Constants.Gui.SetName.ID => new GuiSetName(world.getTileEntity(new BlockPos(x, y, z)))
		case Constants.Gui.EditSource.ID => new GuiEditSource(world.getTileEntity(new BlockPos(x, y, z)))
		case _ => null
	}

	@SideOnly(Side.SERVER)
	override def getServerGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = id match
	{
		case Constants.Gui.SetName.ID => new ContainerSetName(world.getTileEntity(new BlockPos(x, y, z)))
		case Constants.Gui.EditSource.ID => new ContainerEditSource(world.getTileEntity(new BlockPos(x, y, z)))
		case _ => null
	}
}
