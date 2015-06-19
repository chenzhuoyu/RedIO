package org.oxygen.redio.gui

import io.netty.buffer.Unpooled
import net.minecraft.client.gui.{GuiButton, GuiTextField}
import net.minecraft.client.resources.I18n
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.input.{Keyboard, Mouse}
import org.oxygen.redio.common.Constants
import org.oxygen.redio.gui.components.GuiTextBox
import org.oxygen.redio.gui.containers.ContainerEditSource
import org.oxygen.redio.tileentities.TileEntityProgrammer

@SideOnly(Side.CLIENT)
class GuiEditSource(val tileEntity: TileEntity) extends GuiBase(new ContainerEditSource(tileEntity))
{
	private var loaded: Boolean = false
	private var updated: Boolean = false
	private var textName: GuiTextField = null
	private var textSource: GuiTextBox = null
	private var buttonDownload: GuiButton = null

	override def initGui() =
	{
		val programmer = tileEntity.asInstanceOf[TileEntityProgrammer]

		super.initGui()
		Keyboard.enableRepeatEvents(true)

		if (programmer.isEmpty)
		{
			loaded = false
			mc.thePlayer.closeScreen()
		}
		else
		{
			var name = ""
			var script = ""

			if (loaded)
			{
				name = textName.getText
				script = textSource.text
			}

			textName = new GuiTextField(Constants.Gui.EditSource.TEXT_NAME, fontRendererObj, 35, height - 25, 100, 20)
			textSource = new GuiTextBox(Constants.Gui.EditSource.TEXT_SOURCE, 5, 5, width - 10, height - 35)
			buttonDownload = new GuiButton(Constants.Gui.EditSource.BTN_WRITE, width - 60, height - 25, 50, 20, I18n.format("gui.write"))

			addButton(buttonDownload)
			buttonDownload.enabled = false
			textName.setMaxStringLength(16)

			if (loaded)
			{
				textName.setText(name)
				textSource.text = script
			}
			else
			{
				loaded = true
				textName.setText(programmer.name)
				textSource.text = programmer.script
			}
		}
	}

	override def keyTyped(typedChar: Char, keyCode: Int) =
	{
		textName.textboxKeyTyped(typedChar, keyCode)
		textSource.keyEvent(typedChar, keyCode)

		if (keyCode == Keyboard.KEY_ESCAPE)
		{
			updated = false
			mc.thePlayer.closeScreen()
		}
	}

	override def onGuiClosed() =
	{
		super.onGuiClosed()
		Keyboard.enableRepeatEvents(false)

		if (loaded)
		{
			val buffer = new PacketBuffer(Unpooled.buffer())

			buffer.writeString(textName.getText)
			buffer.writeString(textSource.text)
			buffer.writeBoolean(updated)
			buffer.writeBlockPos(tileEntity.getPos)
			mc.getNetHandler.addToSendQueue(new C17PacketCustomPayload(Constants.Gui.EditSource.NAME, buffer))

			if (!updated)
				tileEntity.asInstanceOf[TileEntityProgrammer].ejectMemory()
			else
				tileEntity.asInstanceOf[TileEntityProgrammer].updateScript(textName.getText, textSource.text)
		}
	}

	override def mouseClicked(mouseX: Int, mouseY: Int, button: Int) =
	{
		super.mouseClicked(mouseX, mouseY, button)
		textName.mouseClicked(mouseX, mouseY, button)
		textSource.mouseClicked(mouseX, mouseY, button)
	}

	override def mouseReleased(mouseX: Int, mouseY: Int, button: Int) =
	{
		super.mouseReleased(mouseX, mouseY, button)
		textSource.mouseReleased(mouseX, mouseY, button)
	}

	override def mouseClickMove(mouseX: Int, mouseY: Int, button: Int, dt: Long) =
	{
		super.mouseClickMove(mouseX, mouseY, button, dt)
		textSource.mouseDragged(mouseX, mouseY, button)
	}

	override def handleMouseInput() =
	{
		val dw = Mouse.getDWheel
		super.handleMouseInput()

		if (dw != 0)
		{
			val mouseX = Mouse.getEventX * width / mc.displayWidth
			val mouseY = height - Mouse.getEventY * height / mc.displayHeight - 1

			textSource.mouseWheel(mouseX, mouseY, dw)
		}
	}

	override def drawComponents(mouseX: Int, mouseY: Int, ticks: Float) =
	{
		textName.drawTextBox()
		textSource.drawTextBox()
		super.drawComponents(mouseX, mouseY, ticks)

		buttonDownload.enabled = !textName.getText.isEmpty
		drawString(fontRendererObj, I18n.format("gui.name"), 5, height - 19, 0xffffffff)
		drawString(fontRendererObj, textSource.clipX + "x" + textSource.clipY + "  " +
			(textSource.cursorY + 1) + ":" + (textSource.cursorX + 1), 150, height - 19, 0xffffffff)
	}

	override def actionPerformed(button: GuiButton) = if (button.enabled) button.id match
	{
		case Constants.Gui.EditSource.BTN_WRITE =>
			updated = true
			mc.thePlayer.closeScreen()

		case _ =>
	}
}
