package org.oxygen.redio.gui.components

import net.minecraft.client.gui.Gui

class GuiScrollBar(val x: Int, val y: Int, val width: Int, val height: Int, val horizontal: Boolean) extends Gui
{
	var max = 1.0
	var pos = 0.0
	var value = 0.0

	def drawScrollBar() =
	{
		val h = value / max * height
		val dy = (pos / height) * (height - h)
		Gui.drawRect(x, y + dy.toInt + 2, x + width - 2, y + (dy + h).toInt - 4, 0xffb0b0b0)
	}
}
