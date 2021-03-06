package org.oxygen.redio

import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.IReloadableResourceManager
import net.minecraft.client.resources.model.{ModelBakery, ModelResourceLocation}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.apache.logging.log4j.LogManager
import org.oxygen.redio.blocks._
import org.oxygen.redio.common.{Constants, Utils}
import org.oxygen.redio.gui.GuiHandler
import org.oxygen.redio.items._
import org.oxygen.redio.runtime.ScriptEngine
import org.oxygen.redio.tileentities.{TileEntityPort, TileEntityProcessor, TileEntityProgrammer}

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.MOD_VER, modLanguage = "scala")
object RedIO
{
	val logger = LogManager.getLogger("RedIO")
	val player = Utils.getPrivateField(classOf[NetworkDispatcher], "player")

	@SideOnly(Side.CLIENT)
	def registerFont() =
	{
		Minecraft.getMinecraft.getResourceManager.asInstanceOf[IReloadableResourceManager].registerReloadListener(MonoFontRenderer)
		Minecraft.getMinecraft.refreshResources()
	}

	@SideOnly(Side.CLIENT)
	def registerItem(item: Item, meta: Int, variant: String) =
	{
		val resource = new ModelResourceLocation(variant, "inventory")

		ModelBakery.addVariantName(item, variant)
		ModelLoader.setCustomModelResourceLocation(item, meta, resource)
	}

	@SideOnly(Side.CLIENT)
	def registerBlockItem(block: Block, meta: Int, variant: String) =
	{
		val item = Item.getItemFromBlock(block)
		val resource = new ModelResourceLocation(variant, "inventory")

		ModelBakery.addVariantName(item, variant)
		ModelLoader.setCustomModelResourceLocation(item, meta, resource)
	}

	@EventHandler
	def preinit(event: FMLPreInitializationEvent) =
	{
		/* script engine */
		ScriptEngine.init()

		/* method/field name mapper */
		Constants.NameMapper.init()

		/* items */
		GameRegistry.registerItem(ItemMemory, "memory")
		GameRegistry.registerItem(ItemScreen, "screen")
		GameRegistry.registerItem(ItemSocket, "socket")
		GameRegistry.registerItem(ItemKeyboard, "keyboard")
		GameRegistry.registerItem(ItemMotherboard, "motherboard")
		GameRegistry.registerItem(ItemComputeCore, "compute_core")

		/* normal blocks */
		GameRegistry.registerBlock(BlockPort, "port")
		GameRegistry.registerBlock(BlockCable, "cable")
		GameRegistry.registerBlock(BlockProgrammer, "programmer")

		/* multi-texture blocks */
		GameRegistry.registerBlock(BlockHeatSink, classOf[ItemHeatSink], "heatsink")
		GameRegistry.registerBlock(BlockProcessor, classOf[ItemProcessor], "processor")

		/* tile entities */
		TileEntity.addMapping(classOf[TileEntityPort], "Port")
		TileEntity.addMapping(classOf[TileEntityProcessor], "Processor")
		TileEntity.addMapping(classOf[TileEntityProgrammer], "Programmer")

		/* GUI */
		NetworkRegistry.INSTANCE.registerGuiHandler(Constants.MOD_ID, GuiHandler)

		/* network channels */
		NetworkRegistry.INSTANCE.newEventDrivenChannel(Constants.Gui.SetName.NAME).register(this)
		NetworkRegistry.INSTANCE.newEventDrivenChannel(Constants.Gui.EditSource.NAME).register(this)

		/* recipes */
		Utils.addCraftingRecipe(
			ItemMemory, 1,
			Constants.Meta.NORMAL,
			null          , Items.redstone, null          ,
			Items.redstone, Items.diamond , Items.redstone,
			null          , Items.redstone, null          )

		Utils.addCraftingRecipe(
			ItemScreen, 1,
			Constants.Meta.NORMAL,
			Items.redstone   , Items.glowstone_dust, new ItemStack(Items.dye, 1, 4),
			Items.redstone   , Items.glowstone_dust, new ItemStack(Items.dye, 1, 4),
			Blocks.glass_pane, Blocks.glass_pane   , Blocks.glass_pane             )

		Utils.addCraftingRecipe(
			ItemSocket, 2,
			Constants.Meta.NORMAL,
			null          , null          , null          ,
			Items.redstone, Items.redstone, Items.redstone,
			Items.repeater, Items.repeater, Items.repeater)

		Utils.addCraftingRecipe(
			ItemKeyboard, 1,
			Constants.Meta.NORMAL,
			Blocks.stone_button, Blocks.stone_button, Blocks.stone_button,
			Blocks.stone_button, Blocks.stone_button, Blocks.stone_button,
			Items.iron_ingot   , Items.iron_ingot   , Items.iron_ingot   )

		Utils.addCraftingRecipe(
			ItemMotherboard, 1,
			Constants.Meta.NORMAL,
			null            , null            , null            ,
			Items.redstone  , ItemComputeCore , Items.redstone  ,
			Items.iron_ingot, Items.iron_ingot, Items.iron_ingot)

		Utils.addCraftingRecipe(
			ItemComputeCore, 1,
			Constants.Meta.NORMAL,
			Items.comparator, Items.repeater       , Items.comparator,
			Items.repeater  , Blocks.redstone_block, Items.repeater  ,
			Items.comparator, Items.repeater       , Items.comparator)

		Utils.addCraftingRecipe(
			Item.getItemFromBlock(BlockPort), 1,
			Constants.Meta.NORMAL,
			Items.iron_ingot, ItemSocket     , Items.iron_ingot,
			ItemSocket      , ItemMotherboard, ItemSocket      ,
			Items.iron_ingot, ItemSocket     , Items.iron_ingot)

		Utils.addCraftingRecipe(
			Item.getItemFromBlock(BlockCable), 3,
			Constants.Meta.NORMAL,
			Items.redstone, Blocks.iron_bars, Items.redstone,
			Items.redstone, Blocks.iron_bars, Items.redstone,
			Items.redstone, Blocks.iron_bars, Items.redstone)

		Utils.addCraftingRecipe(
			Item.getItemFromBlock(BlockCable), 3,
			Constants.Meta.NORMAL,
			Items.redstone  , Items.redstone  , Items.redstone  ,
			Blocks.iron_bars, Blocks.iron_bars, Blocks.iron_bars,
			Items.redstone  , Items.redstone  , Items.redstone  )

		Utils.addCraftingRecipe(
			Item.getItemFromBlock(BlockHeatSink), 1,
			Constants.Meta.NORMAL,
			Items.iron_ingot, null            , Items.iron_ingot,
			null            , Items.iron_ingot, null            ,
			Items.gold_ingot, Items.gold_ingot, Items.gold_ingot)

		Utils.addCraftingRecipe(
			Item.getItemFromBlock(BlockHeatSink), 1,
			Constants.Meta.DAMAGED,
			Items.gold_ingot, null            , Items.gold_ingot,
			null            , Items.gold_ingot, null            ,
			Items.gold_ingot, Items.gold_ingot, Items.gold_ingot)

		Utils.addCraftingRecipe(
			Item.getItemFromBlock(BlockProcessor), 1,
			Constants.Meta.NORMAL,
			Items.iron_ingot, BlockCable     , Items.iron_ingot,
			BlockCable      , ItemComputeCore, BlockCable      ,
			Items.iron_ingot, ItemSocket     , Items.iron_ingot)

		Utils.addCraftingRecipe(
			Item.getItemFromBlock(BlockProgrammer), 1,
			Constants.Meta.NORMAL,
			Items.iron_ingot, ItemScreen     , Items.iron_ingot,
			null            , ItemMotherboard, null            ,
			Items.iron_ingot, ItemKeyboard   , Items.iron_ingot)

		/* client side stuffs */
		if (event.getSide.isClient)
		{
			/* font renderer */
			registerFont()

			/* items */
			registerItem(ItemMemory, Constants.Meta.NORMAL, "redio:memory")
			registerItem(ItemScreen, Constants.Meta.NORMAL, "redio:screen")
			registerItem(ItemSocket, Constants.Meta.NORMAL, "redio:socket")
			registerItem(ItemKeyboard, Constants.Meta.NORMAL, "redio:keyboard")
			registerItem(ItemMotherboard, Constants.Meta.NORMAL, "redio:motherboard")
			registerItem(ItemComputeCore, Constants.Meta.NORMAL, "redio:compute_core")

			/* normal blocks */
			registerBlockItem(BlockPort, Constants.Meta.NORMAL, "redio:port")
			registerBlockItem(BlockCable, Constants.Meta.NORMAL, "redio:cable")
			registerBlockItem(BlockProgrammer, Constants.Meta.NORMAL, "redio:programmer")

			/* multi-texture blocks */
			registerBlockItem(BlockHeatSink, Constants.Meta.NORMAL, "redio:heatsink_iron")
			registerBlockItem(BlockHeatSink, Constants.Meta.DAMAGED, "redio:heatsink_gold")
			registerBlockItem(BlockProcessor, Constants.Meta.NORMAL, "redio:processor_intact")
			registerBlockItem(BlockProcessor, Constants.Meta.DAMAGED, "redio:processor_damaged")
		}
	}

	@SubscribeEvent
	def onServerPacket(event: ServerCustomPacketEvent) = event.packet.channel() match
	{
		case Constants.Gui.SetName.NAME =>
			val buffer = new PacketBuffer(event.packet.payload())
			val worldObj = player.get(event.packet.getDispatcher).asInstanceOf[EntityPlayer].worldObj

			val nbt = new NBTTagCompound
			val name = buffer.readStringFromBuffer(32)
			val entity = worldObj.getTileEntity(buffer.readBlockPos())

			entity.writeToNBT(nbt)
			nbt.setString("name", name)
			entity.readFromNBT(nbt)
			entity.markDirty()
			worldObj.markBlockForUpdate(entity.getPos)

		case Constants.Gui.EditSource.NAME =>
			val buffer = new PacketBuffer(event.packet.payload())
			val worldObj = player.get(event.packet.getDispatcher).asInstanceOf[EntityPlayer].worldObj

			val name = buffer.readStringFromBuffer(32)
			val script = buffer.readStringFromBuffer(102400)

			if (!buffer.readBoolean())
				worldObj.getTileEntity(buffer.readBlockPos()).asInstanceOf[TileEntityProgrammer].ejectMemory()
			else
				worldObj.getTileEntity(buffer.readBlockPos()).asInstanceOf[TileEntityProgrammer].updateScript(name, script)
	}
}
