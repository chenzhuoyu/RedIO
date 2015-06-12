package org.oxygen.redio.blocks

import java.util
import java.util.Random

import net.minecraft.block.material.Material
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.block.{Block, ITileEntityProvider}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{BlockPos, EnumFacing}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler
import org.oxygen.redio.common.{Constants, Utils}
import org.oxygen.redio.items.ItemMemory
import org.oxygen.redio.runtime.{PacketNode, PacketType}
import org.oxygen.redio.tileentities.TileEntityProcessor

object BlockProcessor extends BlockBase(Material.rock) with ITileEntityProvider with PacketNode
{
	setHardness(4.0f)
	setResistance(3.0f)
	setHarvestLevel("pickaxe", 1)
	setUnlocalizedName("processor")

	setDefaultState(blockState.getBaseState
		.withProperty(FACING, EnumFacing.NORTH)
		.withProperty(DAMAGED, false)
		.withProperty(WORKING, false))

	override def updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) =
	{
		val te = worldIn.getTileEntity(pos).asInstanceOf[TileEntityProcessor]
		val heatsink = worldIn.getBlockState(pos.up())
		var isDamaged = state.getValue(DAMAGED).asInstanceOf[Boolean]
		val isWorking = (te.inside != null) && (worldIn.isBlockIndirectlyGettingPowered(pos) > 0)

		if (isWorking)
			te.triggerSysTick()

		if (!isWorking)
			te.heat -= 1
		else if (!isDamaged)
			te.heat += 1
		else
			te.heat += 2

		if (heatsink.getBlock == BlockHeatSink)
		{
			if (heatsink.getValue(DAMAGED).asInstanceOf[Boolean])
				te.heat -= 2
			else
				te.heat -= 1
		}

		if (te.heat < 0)
			te.heat = 0

		if (te.heat >= 60)
			isDamaged = true

		if (te.heat >= 80)
			worldIn.addBlockEvent(pos, this, Constants.Events.SMOKE, 0)

		if (te.heat >= 100)
		{
			worldIn.setBlockToAir(pos)
			worldIn.createExplosion(null, pos.getX + 0.5, pos.getY + 0.5, pos.getZ + 0.5, 3.0f, true)
		}
		else
		{
			worldIn.setBlockState(pos, state.withProperty(DAMAGED, isDamaged).withProperty(WORKING, isWorking))
			worldIn.scheduleUpdate(pos, this, tickRate(worldIn))
			worldIn.updateComparatorOutputLevel(pos, this)
		}
	}

	override def getSubBlocks(item: Item, tab: CreativeTabs, subitems: util.List[_]) =
	{
		val items = subitems.asInstanceOf[util.List[ItemStack]]
		items.add(new ItemStack(this, 1, Constants.Meta.NORMAL ))
		items.add(new ItemStack(this, 1, Constants.Meta.DAMAGED))
	}

	override def getMetaFromState(state: IBlockState): Int =
		state.getValue(FACING).asInstanceOf[EnumFacing].getHorizontalIndex +
		(if (state.getValue(DAMAGED).asInstanceOf[Boolean]) Constants.Meta.DAMAGED else Constants.Meta.NORMAL) +
		(if (state.getValue(WORKING).asInstanceOf[Boolean]) Constants.Meta.WORKING else Constants.Meta.NORMAL)

	override def getStateFromMeta(meta: Int): IBlockState = getDefaultState
		.withProperty(DAMAGED, (meta & Constants.Meta.DAMAGED) == Constants.Meta.DAMAGED)
		.withProperty(WORKING, (meta & Constants.Meta.WORKING) == Constants.Meta.WORKING)
		.withProperty(FACING, EnumFacing.getHorizontal(meta & Constants.Meta.FACING_MASK))

	override def tickRate(worldIn: World): Int = 1
	override def createBlockState: BlockState = new BlockState(this, FACING, DAMAGED, WORKING)
	override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileEntityProcessor
	override def hasComparatorInputOverride: Boolean = true

	override def damageDropped(state: IBlockState): Int =
	{
		val isDamaged = state.getValue(DAMAGED).asInstanceOf[Boolean]
		if (isDamaged) Constants.Meta.DAMAGED else Constants.Meta.NORMAL
	}

	override def canConnectRedstone(world: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean =
	{
		val te = world.getTileEntity(pos)
		(te.getBlockMetadata & Constants.Meta.WORKING) == Constants.Meta.WORKING
	}

	override def getComparatorInputOverride(worldIn: World, pos: BlockPos): Int =
	{
		val te = worldIn.getTileEntity(pos)
		te.asInstanceOf[TileEntityProcessor].heat.toInt * 16 / 100
	}

	override def breakBlock(worldIn: World, pos: BlockPos, state: IBlockState): Unit =
	{
		val heatsink = worldIn.getBlockState(pos.up())

		if (heatsink.getBlock == BlockHeatSink)
		{
			worldIn.setBlockToAir(pos.up())
			BlockHeatSink.dropBlockAsItem(worldIn, pos.up(), heatsink, 0)
		}

		worldIn.getTileEntity(pos) match
		{
			case te: TileEntityProcessor =>
				te.asInstanceOf[TileEntityProcessor].ejectMemory()

			case _ =>
		}

		super.breakBlock(worldIn, pos, state)
	}

	override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) =
	{
		val facing = EnumFacing.getHorizontal(Utils.getPlayerFacing(placer))
		val isPowered = worldIn.isBlockIndirectlyGettingPowered(pos) > 0

		worldIn.setBlockState(pos, state.withProperty(FACING, facing).withProperty(WORKING, isPowered))
		worldIn.scheduleUpdate(pos, this, tickRate(worldIn))
		worldIn.updateComparatorOutputLevel(pos, this)

		if (worldIn.isRemote) placer match
		{
			case player: EntityPlayer =>
				FMLNetworkHandler.openGui(player, Constants.MOD_ID,
					Constants.Gui.SetName.ID, worldIn, pos.getX, pos.getY, pos.getZ)

			case _ =>
		}
	}

	override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState,
		playerIn: EntityPlayer, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean =
	{
		worldIn.getTileEntity(pos).asInstanceOf[TileEntityProcessor].ejectMemory()
		(playerIn.getHeldItem == null) || (playerIn.getHeldItem.getItem != ItemMemory)
	}

	override def onNeighborBlockChange(worldIn: World, pos: BlockPos, state: IBlockState, neighborBlock: Block) =
	{
		if (!worldIn.isRemote)
		{
			val isPowered = worldIn.isBlockIndirectlyGettingPowered(pos) > 0
			val wasPowered = state.getValue(WORKING).asInstanceOf[Boolean]

			if (isPowered && !wasPowered)
			{
				worldIn.scheduleUpdate(pos, this, tickRate(worldIn))
				worldIn.updateComparatorOutputLevel(pos, this)
			}
		}
	}

	override def isAcceptable(world: IBlockAccess, pos: BlockPos, name: String): Boolean = world.getTileEntity(pos) match
	{
		case te: TileEntityProcessor => te.name.equals(name)
		case _ => false
	}

	override def acceptPacket(world: IBlockAccess, pos: BlockPos, packet: PacketType): Any = world.getTileEntity(pos) match
	{
		case te: TileEntityProcessor => te.asInstanceOf[TileEntityProcessor].dispatchPacket(packet)
		case _ => null
	}
}
