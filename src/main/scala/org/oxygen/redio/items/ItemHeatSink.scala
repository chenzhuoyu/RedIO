package org.oxygen.redio.items

import com.google.common.base.Function
import net.minecraft.block.Block
import net.minecraft.item.{ItemMultiTexture, ItemStack}
import org.oxygen.redio.common.Constants

class ItemHeatSink(val itemBlock: Block) extends ItemMultiTexture(itemBlock, itemBlock, new Function[ItemStack, String]
{
	override def apply(input: ItemStack): String =
		if ((input.getMetadata & Constants.Meta.DAMAGED) == 0) "iron" else "gold"
})
{
	setUnlocalizedName("heatsink")
	override def getMetadata(damage: Int) = damage
}
