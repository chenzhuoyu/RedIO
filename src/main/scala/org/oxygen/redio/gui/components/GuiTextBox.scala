package org.oxygen.redio.gui.components

import java.awt.Color

import net.minecraft.client.gui.Gui
import org.oxygen.redio.MonoFontRenderer
import org.oxygen.redio.common.Utils

class GuiTextBox(val id: Int, val x: Int, val y: Int, val width: Int, val height: Int) extends Gui
{
	val lines = List[String](
		"func test()",
		"{",
		"    println('hello, worldddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd')",
		"}",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()",
		"wer",
		"wer",
		"wer",
		"wer",
		"wer",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()",
		"test()"
	)

	private var _scrollX = 0
	private var _scrollY = 0
	private var _cursorX = 0
	private var _cursorY = 0

	private val font = MonoFontRenderer.font
	private val scrollV = new GuiScrollBar(x + width - 8, y, 8, height, false)

	def scrollX = _scrollX
	def scrollY = _scrollY
	def cursorX = _cursorX
	def cursorY = _cursorY

	def drawTextBox() =
	{
		val dx = font.measure((lines.length + 1).toString)
		val clipY = (height - 8) / font.height
		val clipX = (width - dx - 16) / font.measure(' ')

		Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0x80000000)
		Utils.strokeRect(x, y, width, height, 0xffa0a0a0)
		drawVerticalLine(x + dx + 4, y, y + height, 0xff303030)

		for ((line, i) <- lines.slice(_scrollY, _scrollY + clipY).zipWithIndex)
		{
			val lineno = (i + _scrollY + 1).toString
			val string = line.substring(_scrollX, Math.min(_scrollX + clipX, line.length))

			font.drawString(string, x + dx + 6, y + i * font.height + 4, Color.white)
			font.drawString(lineno, x + dx - font.measure(lineno) + 2, y + i * font.height + 4, Color.darkGray)
		}

		scrollV.max = lines.length + 1
		scrollV.pos = scrollY
		scrollV.value = Math.min(clipY, lines.length + 1)
		scrollV.drawScrollBar()
	}
}
