package org.oxygen.redio.blocks

import java.util

import net.minecraft.block.BlockDirectional
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{BlockPos, EnumFacing}
import net.minecraft.world.World
import org.oxygen.redio.common.{Utils, Constants}
import org.oxygen.redio.tileentities.TileEntityProcessor

object BlockProcessor extends
{
	val DAMAGED = PropertyBool.create("damaged")
	val WORKING = PropertyBool.create("working")
} with BlockBase(Material.circuits)
{
	setUnlocalizedName("processor")
	TileEntity.addMapping(classOf[TileEntityProcessor], "Processor")

	setDefaultState(blockState.getBaseState
		.withProperty(DAMAGED, false)
		.withProperty(WORKING, false)
		.withProperty(BlockDirectional.FACING, EnumFacing.NORTH))

	override def getSubBlocks(item: Item, tab: CreativeTabs, subitems: util.List[_]) =
	{
		val items = subitems.asInstanceOf[util.List[ItemStack]]
		items.add(new ItemStack(this, 1, Constants.Meta.NORMAL ))
		items.add(new ItemStack(this, 1, Constants.Meta.DAMAGED))
	}

	override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit =
	{
		val facing = EnumFacing.getHorizontal(Utils.getPlayerFacing(placer))
		worldIn.setBlockState(pos, state.withProperty(BlockDirectional.FACING, facing))
	}

	override def getMetaFromState(state: IBlockState): Int =
		(if (state.getValue(DAMAGED).asInstanceOf[Boolean]) Constants.Meta.DAMAGED else 0) +
		(if (state.getValue(WORKING).asInstanceOf[Boolean]) Constants.Meta.WORKING else 0) +
		state.getValue(BlockDirectional.FACING).asInstanceOf[EnumFacing].getHorizontalIndex

	override def getStateFromMeta(meta: Int): IBlockState = getDefaultState
		.withProperty(DAMAGED, (meta & Constants.Meta.DAMAGED) == Constants.Meta.DAMAGED)
		.withProperty(WORKING, (meta & Constants.Meta.WORKING) == Constants.Meta.WORKING)
		.withProperty(BlockDirectional.FACING, EnumFacing.getHorizontal(meta & Constants.Meta.FACING_MASK))

	override def createBlockState: BlockState = new BlockState(this, DAMAGED, WORKING, BlockDirectional.FACING)
	override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileEntityProcessor
}
