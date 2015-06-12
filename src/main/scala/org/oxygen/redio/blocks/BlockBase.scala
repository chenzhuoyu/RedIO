package org.oxygen.redio.blocks

import net.minecraft.block.BlockDirectional
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import org.oxygen.redio.CreativeTab

abstract class BlockBase(material: Material) extends
{
	val FACING = BlockDirectional.FACING
	val DAMAGED = PropertyBool.create("damaged")
	val WORKING = PropertyBool.create("working")
} with BlockDirectional(material)
{
	isBlockContainer = true
	setCreativeTab(CreativeTab)

	override def getMetaFromState(state: IBlockState): Int = 0
	override def getStateFromMeta(meta: Int): IBlockState = getDefaultState

	override def onBlockEventReceived(worldIn: World, pos: BlockPos, state: IBlockState, eventID: Int, eventParam: Int): Boolean =
	{
		super.onBlockEventReceived(worldIn, pos, state, eventID, eventParam)
		worldIn.getTileEntity(pos) match
		{
			case null			=> false
			case te: TileEntity	=> te.receiveClientEvent(eventID, eventParam)
		}
	}
}
