package org.oxygen.redio.common

import java.lang.reflect.{Field, Method}
import java.util

import net.minecraft.block.{BlockRedstoneWire, BlockDirectional}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.item.crafting.CraftingManager
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{EnumFacing, BlockPos, MathHelper}
import net.minecraft.world.World
import net.minecraftforge.oredict.ShapedOreRecipe

import scala.collection.mutable

object Utils
{
	def getPlayerFacing(player: EntityLivingBase): Int =
		MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5d) & 0x03

	def getPrivateField(cls: Class[_], name: String): Field =
	{
		try
		{
			val field = cls.getDeclaredField(name)
			field.setAccessible(true)
			field
		} catch
		{
			case _: NoSuchFieldException =>
				val field = cls.getDeclaredField(Constants.NameMapper.fields.getProperty(cls.getName + "." + name))
				field.setAccessible(true)
				field
		}
	}

	def getPrivateMethod(cls: Class[_], name: String, args: Class[_]*): Method =
	{
		try
		{
			val method = cls.getDeclaredMethod(name, args: _*)
			method.setAccessible(true)
			method
		} catch
		{
			case _: NoSuchMethodException =>
				val method = cls.getDeclaredMethod(Constants.NameMapper.methods.getProperty(cls.getName + "." + name), args: _*)
				method.setAccessible(true)
				method
		}
	}

	def getRedStonePower(worldIn: World, pos: BlockPos): Int =
		EnumFacing.values().map(getRedStonePower(worldIn, pos, _)).sum

	def getRedStonePower(worldIn: World, pos: BlockPos, facing: EnumFacing): Int =
	{
		val powerValue = worldIn.getRedstonePower(pos.offset(facing), facing)

		if (powerValue >= 15)
			return powerValue

		val redstone: IBlockState = worldIn.getBlockState(pos.offset(facing))
		Math.max(powerValue, if (redstone.getBlock == Blocks.redstone_wire)
			redstone.getValue(BlockRedstoneWire.POWER).asInstanceOf[Int] else 0)
	}

	def addCraftingRecipe(result: Item, count: Int, recipe: Item*): Unit =
	{
		if (recipe.length == 9)
		{
			var name = 'A'
			var pattern = ""

			val args = mutable.ArrayBuilder.make[Any]()
			val mapping = mutable.HashMap[Item, Char]()

			for (value <- recipe) value match
			{
				case null => pattern += ' '
				case item => pattern += mapping.getOrElseUpdate(item,
				{
					name = (name + 1).toChar
					name
				})
			}

			args += pattern.substring(0, 3)
			args += pattern.substring(3, 6)
			args += pattern.substring(6, 9)

			for (ident <- mapping.keySet)
			{
				if (ident != null)
				{
					args += mapping(ident)
					args += ident
				}
			}

			CraftingManager.getInstance().getRecipeList.asInstanceOf[util.List[Any]].add(
				new ShapedOreRecipe(new ItemStack(result, count), args.result().asInstanceOf[Array[AnyRef]]:_*))
		}
	}
}
