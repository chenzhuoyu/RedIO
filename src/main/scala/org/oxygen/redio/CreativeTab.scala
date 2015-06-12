package org.oxygen.redio

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import org.oxygen.redio.blocks.BlockProcessor
import org.oxygen.redio.common.Constants

object CreativeTab extends CreativeTabs(Constants.MOD_NAME)
{
	override def getTabIconItem: Item = Item.getItemFromBlock(BlockProcessor)
}
