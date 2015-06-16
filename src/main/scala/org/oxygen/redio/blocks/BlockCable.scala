package org.oxygen.redio.blocks

import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import org.oxygen.redio.runtime.{PacketNode, PacketType}

object BlockCable extends
{
	val UP = PropertyBool.create("up")
	val DOWN = PropertyBool.create("down")
	val EAST = PropertyBool.create("east")
	val WEST = PropertyBool.create("west")
	val NORTH = PropertyBool.create("north")
	val SOUTH = PropertyBool.create("south")
} with BlockBase(Material.circuits) with PacketNode
{
	setResistance(1.0f)
	setUnlocalizedName("cable")

	setDefaultState(blockState.getBaseState
		.withProperty(UP, false)
		.withProperty(DOWN, false)
		.withProperty(EAST, false)
		.withProperty(WEST, false)
		.withProperty(NORTH, false)
		.withProperty(SOUTH, false))

	override def isFullCube: Boolean = false
	override def isOpaqueCube: Boolean = false
	override def createBlockState: BlockState = new BlockState(this, UP, DOWN, EAST, WEST, NORTH, SOUTH)

	override def isAcceptable(world: IBlockAccess, pos: BlockPos, target: String): Boolean = false
	override def acceptPacket(world: IBlockAccess, pos: BlockPos, source: String, packet: PacketType): Any = null

	override def getActualState(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): IBlockState = state
		.withProperty(UP, worldIn.getBlockState(pos.up()).getBlock.isInstanceOf[PacketNode])
		.withProperty(DOWN, worldIn.getBlockState(pos.down()).getBlock.isInstanceOf[PacketNode])
		.withProperty(EAST, worldIn.getBlockState(pos.east()).getBlock.isInstanceOf[PacketNode])
		.withProperty(WEST, worldIn.getBlockState(pos.west()).getBlock.isInstanceOf[PacketNode])
		.withProperty(NORTH, worldIn.getBlockState(pos.north()).getBlock.isInstanceOf[PacketNode])
		.withProperty(SOUTH, worldIn.getBlockState(pos.south()).getBlock.isInstanceOf[PacketNode])
}
