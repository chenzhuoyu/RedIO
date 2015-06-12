package org.oxygen.redio.gui

import java.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.{GuiButton, GuiLabel}
import net.minecraft.client.renderer.{GlStateManager, OpenGlHelper, RenderHelper}
import net.minecraft.inventory.{Container, Slot}
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import org.oxygen.redio.common.Utils

class GuiBase(val container: Container) extends GuiContainer(container)
{
	val theSlot = Utils.getPrivateField(classOf[GuiContainer], "theSlot")
	val touchUpX = Utils.getPrivateField(classOf[GuiContainer], "touchUpX")
	val touchUpY = Utils.getPrivateField(classOf[GuiContainer], "touchUpY")
	val draggedStack = Utils.getPrivateField(classOf[GuiContainer], "draggedStack")
	val returningStack = Utils.getPrivateField(classOf[GuiContainer], "returningStack")
	val isRightMouseClick = Utils.getPrivateField(classOf[GuiContainer], "isRightMouseClick")
	val returningStackTime = Utils.getPrivateField(classOf[GuiContainer], "returningStackTime")
	val dragSplittingRemnant = Utils.getPrivateField(classOf[GuiContainer], "dragSplittingRemnant")
	val returningStackDestSlot = Utils.getPrivateField(classOf[GuiContainer], "returningStackDestSlot")

	val drawSlot = Utils.getPrivateMethod(classOf[GuiContainer], "drawSlot", classOf[Slot])
	val drawItemStack = Utils.getPrivateMethod(classOf[GuiContainer],
		"drawItemStack", classOf[ItemStack], classOf[Int], classOf[Int], classOf[String])

	def addLabel(label: GuiLabel) = labelList.asInstanceOf[util.List[GuiLabel]].add(label)
	def addButton(button: GuiButton) = buttonList.asInstanceOf[util.List[GuiButton]].add(button)

	def isMouseInSlot(slot: Slot, mouseX: Int, mouseY: Int): Boolean =
		isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY)

	def drawComponents(mouseX: Int, mouseY: Int, ticks: Float) =
	{
		labelList.toArray.foreach(_.asInstanceOf[GuiLabel].drawLabel(mc, mouseX, mouseY))
		buttonList.toArray.foreach(_.asInstanceOf[GuiButton].drawButton(mc, mouseX, mouseY))
	}

	/* re-implement drawScreen to provide polymorphic */
	override def drawScreen(mouseX: Int, mouseY: Int, ticks: Float) =
	{
		drawDefaultBackground()
		drawGuiContainerBackgroundLayer(ticks, mouseX, mouseY)
		GlStateManager.disableRescaleNormal()
		RenderHelper.disableStandardItemLighting()
		GlStateManager.disableLighting()
		GlStateManager.disableDepth()
		drawComponents(mouseX, mouseY, ticks)
		RenderHelper.enableGUIStandardItemLighting()
		GlStateManager.pushMatrix()
		GlStateManager.translate(guiLeft, guiTop, 0.0F)
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
		GlStateManager.enableRescaleNormal()

		theSlot.set(this, null)
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)

		for (item <- inventorySlots.inventorySlots.toArray)
		{
			drawSlot.invoke(this, item)
			val slot = item.asInstanceOf[Slot]

			if (slot.canBeHovered && isMouseInSlot(slot, mouseX, mouseY))
			{
				theSlot.set(this, item)
				GlStateManager.disableLighting()
				GlStateManager.disableDepth()
				GlStateManager.colorMask(true, true, true, false)
				drawGradientRect(slot.xDisplayPosition, slot.yDisplayPosition,
					slot.xDisplayPosition + 16, slot.yDisplayPosition + 16, -2130706433, -2130706433)
				GlStateManager.colorMask(true, true, true, true)
				GlStateManager.enableLighting()
				GlStateManager.enableDepth()
			}
		}

		RenderHelper.disableStandardItemLighting()
		drawGuiContainerForegroundLayer(mouseX, mouseY)
		RenderHelper.enableGUIStandardItemLighting()

		var itemStack = if (draggedStack.get(this) == null)
			mc.thePlayer.inventory.getItemStack else draggedStack.get(this).asInstanceOf[ItemStack]

		if (itemStack != null)
		{
			var s: String = null
			val offset = if (draggedStack.get(this) == null) 8 else 16

			if (draggedStack.get(this) != null && isRightMouseClick.getBoolean(this))
			{
				itemStack = itemStack.copy()
				itemStack.stackSize /= 2
			}
			else if (dragSplitting && dragSplittingSlots.size() > 1)
			{
				itemStack = itemStack.copy()
				itemStack.stackSize = dragSplittingRemnant.getInt(this)

				if (itemStack.stackSize == 0)
					s = EnumChatFormatting.YELLOW + "0"
			}

			drawItemStack.invoke(this, itemStack, mouseX - guiLeft - 8: Integer, mouseY - guiTop - offset: Integer, s)
		}

		if (returningStack.get(this) != null)
		{
			var dt = (Minecraft.getSystemTime - returningStackTime.getLong(this)) / 100.0f
			val descSlot = returningStackDestSlot.get(this).asInstanceOf[Slot]

			if (dt >= 1.0f)
			{
				dt = 1.0f
				returningStack.set(this, null)
			}

			val tx = touchUpX.getInt(this)
			val ty = touchUpY.getInt(this)
			val drawX = tx + ((descSlot.xDisplayPosition - tx) * dt).toInt
			val drawY = ty + ((descSlot.yDisplayPosition - ty) * dt).toInt
			drawItemStack.invoke(this, returningStack.get(this), drawX: Integer, drawY: Integer, null)
		}

		GlStateManager.popMatrix()
		val slot = theSlot.get(this).asInstanceOf[Slot]

		if (slot != null && slot.getHasStack && mc.thePlayer.inventory.getItemStack == null)
			renderToolTip(slot.getStack, mouseX, mouseY)

		GlStateManager.enableDepth()
		GlStateManager.enableLighting()
		RenderHelper.enableStandardItemLighting()
	}

	override def drawGuiContainerBackgroundLayer(ticks: Float, mouseX: Int, mouseY: Int): Unit = {}
}
