package org.oxygen.redio.blocks

import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{ChatComponentTranslation, BlockPos, EnumFacing}
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler
import org.oxygen.redio.common.{Constants, Utils}
import org.oxygen.redio.items.ItemMemory
import org.oxygen.redio.tileentities.TileEntityProgrammer

object BlockProgrammer extends BlockBase(Constants.Materials.Programmer) with ITileEntityProvider
{
	setHardness(4.0f)
	setResistance(3.0f)
	setHarvestLevel("pickaxe", 1)
	setUnlocalizedName("programmer")

	setDefaultState(blockState.getBaseState.withProperty(FACING, EnumFacing.NORTH))

	override def isFullCube: Boolean = false
	override def isOpaqueCube: Boolean = false
	override def createBlockState: BlockState = new BlockState(this, FACING)
	override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileEntityProgrammer

	override def getMetaFromState(state: IBlockState): Int = state.getValue(FACING).asInstanceOf[EnumFacing].getHorizontalIndex
	override def getStateFromMeta(meta: Int): IBlockState = getDefaultState.withProperty(FACING, EnumFacing.getHorizontal(meta))

	override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) =
		worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getHorizontal(Utils.getPlayerFacing(placer))))

	override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState,
		playerIn: EntityPlayer, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean =
	{
		if (playerIn.getHeldItem != null && playerIn.getHeldItem.getItem == ItemMemory)
		{
			val te = worldIn.getTileEntity(pos).asInstanceOf[TileEntityProgrammer]

			if (te.memory != null)
			{
				if (worldIn.isRemote)
					playerIn.addChatMessage(new ChatComponentTranslation("chat.programmer.occupied"))
			}
			else
			{
				te.memory = playerIn.getHeldItem

				if (worldIn.isRemote)
					FMLNetworkHandler.openGui(playerIn, Constants.MOD_ID,
						Constants.Gui.EditSource.ID, worldIn, pos.getX, pos.getY, pos.getZ)
			}
		}

		true
	}
}
