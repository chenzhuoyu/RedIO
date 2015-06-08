package org.oxygen.redio.blocks

import java.util

import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{BlockPos, EnumFacing}
import net.minecraft.world.World
import org.oxygen.redio.common.{Constants, Utils}
import org.oxygen.redio.tileentities.TileEntityProcessor

object BlockHeatSink extends BlockBase(Constants.Materials.HeatSink)
{
	setHardness(4.0f)
	setResistance(3.0f)
	setHarvestLevel("pickaxe", 1)
	setUnlocalizedName("heatsink")

	setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.375f, 1.0f)
	setDefaultState(blockState.getBaseState
		.withProperty(FACING, EnumFacing.NORTH)
		.withProperty(DAMAGED, false))

	override def isFullCube: Boolean = false
	override def isOpaqueCube: Boolean = false
	override def createBlockState: BlockState = new BlockState(this, FACING, DAMAGED)

	override def getSubBlocks(item: Item, tab: CreativeTabs, subitems: util.List[_]) =
	{
		val items = subitems.asInstanceOf[util.List[ItemStack]]
		items.add(new ItemStack(this, 1, Constants.Meta.NORMAL ))
		items.add(new ItemStack(this, 1, Constants.Meta.DAMAGED))
	}

	override def damageDropped(state: IBlockState): Int =
	{
		val isDamaged = state.getValue(DAMAGED).asInstanceOf[Boolean]
		if (isDamaged) Constants.Meta.DAMAGED else Constants.Meta.NORMAL
	}

	override def canPlaceBlockAt(worldIn: World, pos: BlockPos): Boolean =
		worldIn.getTileEntity(pos.down()).isInstanceOf[TileEntityProcessor]

	override def getMetaFromState(state: IBlockState): Int =
		state.getValue(FACING).asInstanceOf[EnumFacing].getHorizontalIndex +
		(if (state.getValue(DAMAGED).asInstanceOf[Boolean]) Constants.Meta.DAMAGED else Constants.Meta.NORMAL)

	override def getStateFromMeta(meta: Int): IBlockState = getDefaultState
		.withProperty(DAMAGED, (meta & Constants.Meta.DAMAGED) == Constants.Meta.DAMAGED)
		.withProperty(FACING, EnumFacing.getHorizontal(meta & Constants.Meta.FACING_MASK))

	override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) =
		worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getHorizontal(Utils.getPlayerFacing(placer))))
}
