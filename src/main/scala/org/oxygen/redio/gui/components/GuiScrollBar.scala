package org.oxygen.redio.gui.components

import net.minecraft.client.gui.Gui

class GuiScrollBar(val x: Int, val y: Int, val width: Int, val height: Int, val horizontal: Boolean) extends Gui
{
	var max = 1.0
	var pos = 0.0
	var value = 0.0

	private var clicked = false
	private var clickPos = -1

	private def bound = if (horizontal) width else height
	private def limit(pos: Double, h: Double) = Math.min(Math.max(pos, 0), max - h * max / bound)
	private def isInside(mouseX: Int, mouseY: Int) = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height

	def isClicked = clicked

	def drawScrollBar() =
	{
		val h = value / max * bound
		val d = limit(pos, h) / max * bound

		if (!horizontal)
			Gui.drawRect(x, y + d.toInt + 2, x + width - 2, y + (d + h).toInt - 2, 0xffb0b0b0)
		else
			Gui.drawRect(x + d.toInt + 2, y, x + (d + h).toInt - 2, y + height - 2, 0xffb0b0b0)
	}

	def mouseWheel(mouseX: Int, mouseY: Int, delta: Double) =
	{
		val h = value / max * bound
		pos = limit(pos - delta / 12.0, h)
	}

	def mouseClicked(mouseX: Int, mouseY: Int, button: Int) =
	{
		if (button == 0 && isInside(mouseX, mouseY))
		{
			clicked = true
			clickPos = if (horizontal) mouseX else mouseY
		}
	}

	def mouseDragged(mouseX: Int, mouseY: Int, button: Int) =
	{
		if (clicked && button == 0)
		{
			val h = value / max * bound
			val p = if (horizontal) mouseX else mouseY

			pos = limit(pos + (p - clickPos) * max / bound, h)
			clickPos = p
		}
	}

	def mouseReleased(mouseX: Int, mouseY: Int, button: Int) =
	{
		if (clicked && button == 0)
		{
			clicked = false
			clickPos = -1
		}
	}
}
