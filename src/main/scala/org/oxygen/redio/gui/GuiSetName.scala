package org.oxygen.redio.gui

import io.netty.buffer.Unpooled
import net.minecraft.client.gui.{GuiButton, GuiTextField}
import net.minecraft.client.resources.I18n
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.tileentity.TileEntity
import org.lwjgl.input.Keyboard
import org.oxygen.redio.common.Constants
import org.oxygen.redio.gui.containers.ContainerSetName

class GuiSetName(val tileEntity: TileEntity) extends GuiBase(new ContainerSetName(tileEntity))
{
	private var buttonOK: GuiButton = null
	private var textName: GuiTextField = null

	override def initGui() =
	{
		super.initGui()
		Keyboard.enableRepeatEvents(true)

		textName = new GuiTextField(Constants.Gui.SetName.TEXT_NAME, fontRendererObj, guiLeft, guiTop + 30, xSize, 20)
		buttonOK = new GuiButton(Constants.Gui.SetName.BTN_OK, (width - 50) / 2, guiTop + 70, 50, 20, I18n.format("gui.done"))

		addButton(buttonOK)
		buttonOK.enabled = false
		textName.setMaxStringLength(32)
	}

	override def keyTyped(typedChar: Char, keyCode: Int) =
	{
		textName.textboxKeyTyped(typedChar, keyCode)
		buttonOK.enabled = !textName.getText.trim.isEmpty

		if (keyCode == Keyboard.KEY_RETURN ||
			keyCode == Keyboard.KEY_NUMPADENTER)
			actionPerformed(buttonOK)
	}

	override def onGuiClosed() =
	{
		val buffer = new PacketBuffer(Unpooled.buffer())

		super.onGuiClosed()
		Keyboard.enableRepeatEvents(false)
		buffer.writeString(textName.getText.trim)
		buffer.writeBlockPos(tileEntity.getPos)
		mc.getNetHandler.addToSendQueue(new C17PacketCustomPayload(Constants.Gui.SetName.NAME, buffer))
	}

	override def mouseClicked(mouseX: Int, mouseY: Int, button: Int) =
	{
		super.mouseClicked(mouseX, mouseY, button)
		textName.mouseClicked(mouseX, mouseY, button)
	}

	override def drawComponents(mouseX: Int, mouseY: Int, ticks: Float) =
	{
		textName.drawTextBox()
		super.drawComponents(mouseX, mouseY, ticks)
		drawCenteredString(fontRendererObj, I18n.format("gui.setname"), width / 2, guiTop + 10, 0xffffff)
	}

	override def actionPerformed(button: GuiButton) = if (button.enabled) button.id match
	{
		case Constants.Gui.SetName.BTN_OK => mc.thePlayer.closeScreen()
		case _ =>
	}
}
