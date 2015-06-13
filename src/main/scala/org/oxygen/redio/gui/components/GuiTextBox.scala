package org.oxygen.redio.gui.components

import java.awt.Color

import net.minecraft.client.gui.{GuiSlider, Gui}
import net.minecraft.client.renderer.{GlStateManager, Tessellator}
import org.lwjgl.opengl.GL11
import org.oxygen.redio.MonoFontRenderer

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
	private val scroll = new GuiScrollBar(0, 0, 0)

	def scrollX = _scrollX
	def scrollY = _scrollY
	def cursorX = _cursorX
	def cursorY = _cursorY

	def strokeRect(x: Int, y: Int, width: Int, height: Int, color: Int) =
	{
		val r = x + width
		val b = y + height
		val tessellator = Tessellator.getInstance
		val worldRenderer = tessellator.getWorldRenderer

		GlStateManager.enableBlend()
		GlStateManager.disableTexture2D()
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
		GlStateManager.color(
			(color >> 16 & 255).toFloat / 255.0f,
			(color >>  8 & 255).toFloat / 255.0f,
			(color >>  0 & 255).toFloat / 255.0f,
			(color >> 24 & 255).toFloat / 255.0f)

		worldRenderer.startDrawing(GL11.GL_LINE_LOOP)
		worldRenderer.addVertex(x.toDouble, b.toDouble, 0.0)
		worldRenderer.addVertex(r.toDouble, b.toDouble, 0.0)
		worldRenderer.addVertex(r.toDouble, y.toDouble, 0.0)
		worldRenderer.addVertex(x.toDouble, y.toDouble, 0.0)

		tessellator.draw()
		GlStateManager.enableTexture2D()
		GlStateManager.disableBlend()
	}

	def drawTextBox() =
	{
		val dx = font.measure((lines.length + 1).toString)
		val clipY = (height - 8) / font.height
		val clipX = (width - dx - 16) / font.measure(' ')

		Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0x80000000)

		strokeRect(x, y, width, height, 0xffa0a0a0)
		drawVerticalLine(x + dx + 4, y, y + height, 0xff303030)

		for ((line, i) <- lines.slice(_scrollY, _scrollY + clipY).zipWithIndex)
		{
			val lineno = (i + _scrollY + 1).toString
			val string = line.substring(_scrollX, Math.min(_scrollX + clipX, line.length))

			font.drawString(string, x + dx + 6, y + i * font.height + 4, Color.white)
			font.drawString(lineno, x + dx - font.measure(lineno) + 2, y + i * font.height + 4, Color.darkGray)
		}
	}
}
