package org.oxygen.redio.gui

import java.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.{GuiButton, GuiLabel, GuiScreen}
import net.minecraft.client.renderer.{GlStateManager, OpenGlHelper, RenderHelper}
import net.minecraft.inventory.{Container, Slot}
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.oxygen.redio.common.Utils

@SideOnly(Side.CLIENT)
class GuiBase(val container: Container) extends GuiContainer(container)
{
	private val theSlot = Utils.getPrivateField(classOf[GuiContainer], "theSlot")
	private val touchUpX = Utils.getPrivateField(classOf[GuiContainer], "touchUpX")
	private val touchUpY = Utils.getPrivateField(classOf[GuiContainer], "touchUpY")
	private val draggedStack = Utils.getPrivateField(classOf[GuiContainer], "draggedStack")
	private val returningStack = Utils.getPrivateField(classOf[GuiContainer], "returningStack")
	private val isRightMouseClick = Utils.getPrivateField(classOf[GuiContainer], "isRightMouseClick")
	private val returningStackTime = Utils.getPrivateField(classOf[GuiContainer], "returningStackTime")
	private val dragSplittingRemnant = Utils.getPrivateField(classOf[GuiContainer], "dragSplittingRemnant")
	private val returningStackDestSlot = Utils.getPrivateField(classOf[GuiContainer], "returningStackDestSlot")

	private val drawSlot = Utils.getPrivateMethod(classOf[GuiContainer], "drawSlot", classOf[Slot])
	private val drawItemStack = Utils.getPrivateMethod(classOf[GuiContainer],
		"drawItemStack", classOf[ItemStack], classOf[Int], classOf[Int], classOf[String])

	private val doubleClick = Utils.getPrivateField(classOf[GuiContainer], "doubleClick")
	private val clickedSlot = Utils.getPrivateField(classOf[GuiContainer], "clickedSlot")
	private val lastClickSlot = Utils.getPrivateField(classOf[GuiContainer], "lastClickSlot")
	private val lastClickTime = Utils.getPrivateField(classOf[GuiContainer], "lastClickTime")
	private val ignoreMouseUp = Utils.getPrivateField(classOf[GuiContainer], "ignoreMouseUp")
	private val lastClickButton = Utils.getPrivateField(classOf[GuiContainer], "lastClickButton")
	private val shiftClickedSlot = Utils.getPrivateField(classOf[GuiContainer], "shiftClickedSlot")
	private val dragSplittingLimit = Utils.getPrivateField(classOf[GuiContainer], "dragSplittingLimit")
	private val dragSplittingButton = Utils.getPrivateField(classOf[GuiContainer], "dragSplittingButton")

	private val selectedButton = Utils.getPrivateField(classOf[GuiScreen], "selectedButton")
	private val getSlotAtPosition = Utils.getPrivateMethod(classOf[GuiContainer], "getSlotAtPosition", classOf[Int], classOf[Int])

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
				GlStateManager.disableDepth()
				GlStateManager.disableLighting()
				GlStateManager.colorMask(true, true, true, false)
				drawGradientRect(slot.xDisplayPosition, slot.yDisplayPosition,
					slot.xDisplayPosition + 16, slot.yDisplayPosition + 16, 0x80ffffff, 0x80ffffff)
				GlStateManager.colorMask(true, true, true, true)
				GlStateManager.enableDepth()
				GlStateManager.enableLighting()
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

	/* re-implement mouseClicked to provide compability with NEI */
	override def mouseClicked(mouseX: Int, mouseY: Int, button: Int): Unit =
	{
		if (button == 0)
		{
			buttonList.toArray.foreach
			{
				case btn: GuiButton if btn.mousePressed(mc, mouseX, mouseY) =>
					selectedButton.set(this, btn)
					btn.playPressSound(mc.getSoundHandler)
					actionPerformed(btn)

				case _ =>
			}
		}

		val time = Minecraft.getSystemTime
		val pick = mc.gameSettings.keyBindPickBlock.getKeyCode + 100
		val slot = getSlotAtPosition.invoke(this, mouseX: Integer, mouseY: Integer).asInstanceOf[Slot]

		ignoreMouseUp.setBoolean(this, false)
		doubleClick.setBoolean(this,
			lastClickSlot.get(this) == slot &&
			lastClickButton.getInt(this) == button &&
			time - lastClickTime.getLong(this) < 250)

		if (button == 0 || button == 1 || button == pick)
		{
			var slotNum = -1
			val isOutside = mouseY < guiTop || mouseX < guiLeft ||
				mouseY >= guiTop + this.ySize || mouseX >= guiLeft + this.xSize

			if (slot != null)
				slotNum = slot.slotNumber

			if (isOutside)
				slotNum = -999

			if (isOutside && mc.gameSettings.touchscreen && mc.thePlayer.inventory.getItemStack == null)
			{
				this.mc.displayGuiScreen(null)
				return
			}

			if (slotNum != -1)
			{
				if (mc.gameSettings.touchscreen)
				{
					if (slot == null || !slot.getHasStack)
					{
						clickedSlot.set(this, null)
					}
					else
					{
						clickedSlot.set(this, slot)
						draggedStack.set(this, null)
						isRightMouseClick.setBoolean(this, button == 1)
					}
				}
				else if (!dragSplitting)
				{
					if (mc.thePlayer.inventory.getItemStack != null)
					{
						dragSplitting = true
						dragSplittingSlots.clear()
						dragSplittingButton.setInt(this, button)

						button match
						{
							case 0 => dragSplittingLimit.setInt(this, 0)
							case 1 => dragSplittingLimit.setInt(this, 1)
							case x if x == pick => dragSplittingLimit.setInt(this, 2)
							case _ =>
						}
					}
					else
					{
						if (button == pick)
						{
							handleMouseClick(slot, slotNum, button, 3)
						}
						else
						{
							if (slotNum == -999)
							{
								handleMouseClick(slot, slotNum, button, 4)
							}
							else if (GuiScreen.isShiftKeyDown)
							{
								if (slot == null || !slot.getHasStack)
									shiftClickedSlot.set(this, null)
								else
									shiftClickedSlot.set(this, slot.getStack)

								handleMouseClick(slot, slotNum, button, 1)
							}
							else
							{
								handleMouseClick(slot, slotNum, button, 0)
							}

							ignoreMouseUp.setBoolean(this, true)
						}
					}
				}
			}
		}

		lastClickSlot.set(this, slot)
		lastClickTime.setLong(this, time)
		lastClickButton.setInt(this, button)
	}

	override def drawGuiContainerBackgroundLayer(ticks: Float, mouseX: Int, mouseY: Int): Unit = {}
}
