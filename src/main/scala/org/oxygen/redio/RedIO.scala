package org.oxygen.redio

import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.IReloadableResourceManager
import net.minecraft.client.resources.model.{ModelBakery, ModelResourceLocation}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.Item
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
import org.oxygen.redio.items.{ItemHeatSink, ItemMemory, ItemProcessor}
import org.oxygen.redio.runtime.ScriptEngine
import org.oxygen.redio.tileentities.{TileEntityProgrammer, TileEntityPort, TileEntityProcessor}

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.MOD_VER, modLanguage = "scala")
object RedIO
{
	val logger = LogManager.getLogger("RedIO")
	val player = Utils.getPrivateField(classOf[NetworkDispatcher], "player")

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

		/* font renderer */
		Minecraft.getMinecraft.getResourceManager.asInstanceOf[IReloadableResourceManager].registerReloadListener(MonoFontRenderer)
		Minecraft.getMinecraft.refreshResources()

		/* items */
		GameRegistry.registerItem(ItemMemory, "memory")

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
		Utils.addCraftingRecipe(ItemMemory, 1,
			null          , Items.redstone, null,
			Items.redstone, Items.diamond , Items.redstone,
			null          , Items.redstone, null)

		/* client item models */
		if (event.getSide.isClient)
		{
			/* normal blocks */
			registerBlockItem(BlockPort, Constants.Meta.NORMAL, "redio:port")
			registerBlockItem(BlockCable, Constants.Meta.NORMAL, "redio:cable")
			registerBlockItem(BlockProgrammer, Constants.Meta.NORMAL, "redio:programmer")

			/* multi-texture blocks */
			registerBlockItem(BlockHeatSink, Constants.Meta.NORMAL, "redio:heatsink_iron")
			registerBlockItem(BlockHeatSink, Constants.Meta.DAMAGED, "redio:heatsink_gold")
			registerBlockItem(BlockProcessor, Constants.Meta.NORMAL, "redio:processor_intact")
			registerBlockItem(BlockProcessor, Constants.Meta.DAMAGED, "redio:processor_damaged")

			/* items */
			ModelLoader.setCustomModelResourceLocation(ItemMemory, 0, new ModelResourceLocation("redio:memory", "inventory"))
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
	}
}
