package org.oxygen.redio.blocks

import net.minecraft.block.material.Material
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.block.{BlockDirectional, ITileEntityProvider}
import net.minecraft.util.EnumFacing
import org.oxygen.redio.CreativeTab
import org.oxygen.redio.common.Constants

abstract class BlockBase(material: Material) extends BlockDirectional(material) with ITileEntityProvider
{
	setCreativeTab(CreativeTab)
	setDefaultState(blockState.getBaseState.withProperty(BlockDirectional.FACING, EnumFacing.NORTH))

	override def getMetaFromState(state: IBlockState): Int =
		state.getValue(BlockDirectional.FACING).asInstanceOf[EnumFacing].getHorizontalIndex

	override def getStateFromMeta(meta: Int): IBlockState = getDefaultState
		.withProperty(BlockDirectional.FACING, EnumFacing.getHorizontal(meta & Constants.Meta.FACING_MASK))

	override def createBlockState: BlockState = new BlockState(this, BlockDirectional.FACING)
}
