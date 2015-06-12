package org.oxygen.redio.items

import com.google.common.base.Function
import net.minecraft.block.Block
import net.minecraft.item.{ItemMultiTexture, ItemStack}
import org.oxygen.redio.common.Constants

class ItemProcessor(val itemBlock: Block) extends ItemMultiTexture(itemBlock, itemBlock, new Function[ItemStack, String]
{
	override def apply(input: ItemStack): String =
		if ((input.getMetadata & Constants.Meta.DAMAGED) == 0) "intact" else "damaged"
})
{
	setUnlocalizedName("processor")
	override def getMetadata(damage: Int) = damage
}
