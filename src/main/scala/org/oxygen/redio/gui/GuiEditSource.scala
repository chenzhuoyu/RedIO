package org.oxygen.redio.gui

import net.minecraft.client.gui.{GuiTextField, GuiButton}
import net.minecraft.client.resources.I18n
import net.minecraft.tileentity.TileEntity
import org.lwjgl.input.Keyboard
import org.oxygen.redio.common.Constants
import org.oxygen.redio.gui.components.GuiTextBox
import org.oxygen.redio.gui.containers.ContainerEditSource

class GuiEditSource(val tileEntity: TileEntity) extends GuiBase(new ContainerEditSource(tileEntity))
{
	private var textName: GuiTextField = null
	private var textSource: GuiTextBox = null
	private var buttonDownload: GuiButton = null

	override def initGui() =
	{
		super.initGui()
		Keyboard.enableRepeatEvents(true)

		textName = new GuiTextField(Constants.Gui.EditSource.TEXT_NAME, fontRendererObj, 35, height - 25, 100, 20)
		textSource = new GuiTextBox(Constants.Gui.EditSource.TEXT_SOURCE, 5, 5, width - 10, height - 35)
		buttonDownload = new GuiButton(Constants.Gui.EditSource.BTN_WRITE, width - 60, height - 25, 50, 20, I18n.format("gui.write"))

		addButton(buttonDownload)
		buttonDownload.enabled = false
		textName.setMaxStringLength(16)
	}

	override def keyTyped(typedChar: Char, keyCode: Int) =
	{
		textName.textboxKeyTyped(typedChar, keyCode)
		buttonDownload.enabled = !textName.getText.trim.isEmpty

		if (keyCode == Keyboard.KEY_ESCAPE)
			mc.thePlayer.closeScreen()
	}

	override def onGuiClosed() =
	{
		super.onGuiClosed()
		Keyboard.enableRepeatEvents(false)
	}

	override def mouseClicked(mouseX: Int, mouseY: Int, button: Int) =
	{
		println(button)
		super.mouseClicked(mouseX, mouseY, button)
		textName.mouseClicked(mouseX, mouseY, button)
	}

	override def drawComponents(mouseX: Int, mouseY: Int, ticks: Float) =
	{
		textName.drawTextBox()
		textSource.drawTextBox()
		super.drawComponents(mouseX, mouseY, ticks)
		drawString(fontRendererObj, I18n.format("gui.name"), 5, height - 19, 0xffffffff)
	}

	override def actionPerformed(button: GuiButton) = if (button.enabled) button.id match
	{
		case Constants.Gui.EditSource.BTN_WRITE => mc.thePlayer.closeScreen()
		case _ =>
	}
}
