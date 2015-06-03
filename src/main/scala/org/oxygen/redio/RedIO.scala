package org.oxygen.redio

import net.minecraft.block.Block
import net.minecraft.client.resources.model.{ModelBakery, ModelResourceLocation}
import net.minecraft.item.Item
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.oxygen.redio.blocks.BlockProcessor
import org.oxygen.redio.common.Constants
import org.oxygen.redio.items.ItemProcessor

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.MOD_VER, modLanguage = "scala")
object RedIO
{
	@SideOnly(Side.CLIENT)
	def registerItem(block: Block, meta: Int, variant: String) =
	{
		val item = Item.getItemFromBlock(block)
		val resource = new ModelResourceLocation(variant, "inventory")

		ModelBakery.addVariantName(item, variant)
		ModelLoader.setCustomModelResourceLocation(item, meta, resource)
	}

	@EventHandler
	def preinit(event: FMLPreInitializationEvent) =
	{
		GameRegistry.registerBlock(BlockProcessor, classOf[ItemProcessor], "processor")

		if (event.getSide.isClient)
		{
			registerItem(BlockProcessor, Constants.Meta.NORMAL, "redio:processor_intact")
			registerItem(BlockProcessor, Constants.Meta.DAMAGED, "redio:processor_damaged")
		}
	}
}
