package org.oxygen.redio.blocks

import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{BlockPos, EnumFacing}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler
import org.oxygen.redio.common.Constants
import org.oxygen.redio.runtime.{PacketNode, PacketType}
import org.oxygen.redio.tileentities.TileEntityPort

object BlockPort extends
{
	val POWERING = PropertyBool.create("powering")
} with BlockBase(Material.iron) with ITileEntityProvider with PacketNode
{
	setHardness(4.0f)
	setResistance(3.0f)
	setHarvestLevel("pickaxe", 1)
	setUnlocalizedName("port")

	setDefaultState(blockState.getBaseState.withProperty(POWERING, false))

	override def canProvidePower: Boolean = true
	override def createBlockState: BlockState = new BlockState(this, POWERING)
	override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileEntityPort

	override def getStateFromMeta(meta: Int): IBlockState = getDefaultState.withProperty(POWERING, meta != 0)
	override def getMetaFromState(state: IBlockState): Int = if (state.getValue(POWERING).asInstanceOf[Boolean]) 1 else 0

	override def isProvidingWeakPower(worldIn: IBlockAccess, pos: BlockPos, state: IBlockState, side: EnumFacing): Int =
		if (state.getValue(POWERING).asInstanceOf[Boolean]) 15 else 0

	override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) =
	{
		if (worldIn.isRemote) placer match
		{
			case player: EntityPlayer =>
				FMLNetworkHandler.openGui(player, Constants.MOD_ID,
					Constants.Gui.SetName.ID, worldIn, pos.getX, pos.getY, pos.getZ)

			case _ =>
		}
	}

	override def isAcceptable(world: IBlockAccess, pos: BlockPos, name: String): Boolean = world.getTileEntity(pos) match
	{
		case te: TileEntityPort => te.name.equals(name)
		case _ => false
	}

	override def acceptPacket(world: IBlockAccess, pos: BlockPos, packet: PacketType): Any = world.getTileEntity(pos) match
	{
		case te: TileEntityPort => te.asInstanceOf[TileEntityPort].dispatchPacket(packet)
		case _ => null
	}
}
