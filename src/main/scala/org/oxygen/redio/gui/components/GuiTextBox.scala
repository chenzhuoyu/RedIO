package org.oxygen.redio.gui.components

import java.awt.Color

import net.minecraft.client.gui.Gui
import org.apache.commons.lang3.StringUtils
import org.lwjgl.input.Keyboard
import org.oxygen.redio.MonoFontRenderer
import org.oxygen.redio.common.Utils

import scala.collection.mutable

class GuiTextBox(val id: Int, val x: Int, val y: Int, val width: Int, val height: Int) extends Gui
{
	val lines = mutable.ListBuffer[String]("")

	private var clipX = 0
	private var clipY = 0

	private var cursorX = 0
	private var cursorY = 0

	private var focused = true
	private var blinkTick = 0L
	private var showCursor = false

	private val font = MonoFontRenderer.font
	private val scrollH = new GuiScrollBar(x, y + height - 8, width, 8, true)
	private val scrollV = new GuiScrollBar(x + width - 8, y, 8, height - 8, false)

	private def isInside(mouseX: Int, mouseY: Int) =
		mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height

	private def updateScroll(updatePos: Boolean) =
	{
		scrollV.max = Math.max(clipY, lines.length)
		scrollH.max = Math.max(clipX, lines.map(_.length).max + 1)
		scrollH.value = clipX
		scrollV.value = clipY

		if (updatePos)
		{
			scrollH.pos = Math.min(Math.min(Math.max(Math.max(scrollH.pos, 0), cursorX - clipX + 1), scrollH.max - clipX), cursorX)
			scrollV.pos = Math.min(Math.min(Math.max(Math.max(scrollV.pos, 0), cursorY - clipY + 1), scrollV.max - clipY), cursorY)
		}
	}

	def text = lines.mkString("\n")
	def text_=(text: String) =
	{
		lines.clear()
		lines.append(text.split("\n"):_*)
	}

	def moveCursor(x: Int, y: Int) =
	{
		blinkTick = 0
		showCursor = false

		synchronized
		{
			cursorY = Math.min(Math.max(y, 0), lines.length - 1)
			cursorX = Math.min(Math.max(x, 0), lines(cursorY).length)
			updateScroll(true)
		}
	}

	def drawTextBox() =
	{
		val cw = font.measure(' ')
		val dx = font.measure(lines.length.toString)

		clipX = (width - dx - 16) / cw
		clipY = (height - 8) / font.height

		Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0x80000000)
		Utils.strokeRect(x, y, width, height, 0xffa0a0a0)
		drawVerticalLine(x + dx + 4, y, y + height - 8, 0xff303030)

		for ((line, i) <- lines.slice(scrollV.pos.toInt, scrollV.pos.toInt + clipY).zipWithIndex)
		{
			val lineno = (i + scrollV.pos.toInt + 1).toString
			val string = if (scrollH.pos.toInt >= line.length) "" else
				line.substring(scrollH.pos.toInt, Math.min(scrollH.pos.toInt + clipX, line.length))

			font.drawString(string, x + dx + 6, y + i * font.height + 4, Color.white)
			font.drawString(lineno, x + dx - font.measure(lineno) + 2, y + i * font.height + 4, Color.darkGray)
		}

		if (System.currentTimeMillis() - blinkTick > 500)
		{
			blinkTick = System.currentTimeMillis()
			showCursor = !showCursor
		}

		if (focused && showCursor)
			drawHorizontalLine(
				x + dx + cw * (cursorX - scrollH.pos.toInt) + 6,
				x + dx + cw * (cursorX - scrollH.pos.toInt + 1) + 4,
				y + font.height * (cursorY - scrollV.pos.toInt + 1) + 2,
				0xffffffff)

		synchronized
		{
			updateScroll(false)
		}

		scrollH.drawScrollBar()
		scrollV.drawScrollBar()
	}

	def keyEvent(char: Char, code: Int) = if (focused) code match
	{
		case Keyboard.KEY_UP	=> moveCursor(cursorX, cursorY - 1)
		case Keyboard.KEY_DOWN	=> moveCursor(cursorX, cursorY + 1)
		case Keyboard.KEY_LEFT	=> moveCursor(cursorX - 1, cursorY)
		case Keyboard.KEY_RIGHT	=> moveCursor(cursorX + 1, cursorY)

		case Keyboard.KEY_HOME	=> moveCursor(0, cursorY)
		case Keyboard.KEY_END	=> moveCursor(lines(cursorY).length, cursorY)

		case Keyboard.KEY_PRIOR =>
			blinkTick = 0
			showCursor = false

			synchronized
			{
				cursorY = Math.max(cursorY - clipY, 0)
				scrollV.pos = Math.max(scrollV.pos - clipY, 0)
			}

		case Keyboard.KEY_NEXT =>
			blinkTick = 0
			showCursor = false

			synchronized
			{
				cursorY = Math.min(cursorY + clipY, lines.length - 1)
				scrollV.pos = Math.min(scrollV.pos + clipY, scrollV.max - clipY)
			}

		case Keyboard.KEY_DELETE =>
			blinkTick = 0
			showCursor = false

			if (cursorX < lines(cursorY).length)
			{
				val sb = new StringBuilder(lines(cursorY))

				sb.deleteCharAt(cursorX)
				lines(cursorY) = sb.mkString
			}
			else if (cursorY < lines.length - 1)
			{
				lines(cursorY) += lines(cursorY + 1)
				lines.remove(cursorY + 1)

				synchronized
				{
					updateScroll(true)
				}
			}

		case Keyboard.KEY_BACK =>
			blinkTick = 0
			showCursor = false

			if (cursorX > 0)
			{
				val sb = new StringBuilder(lines(cursorY))

				moveCursor(cursorX - 1, cursorY)
				sb.deleteCharAt(cursorX)
				lines(cursorY) = sb.mkString
			}
			else if (cursorY > 0)
			{
				val cx = lines(cursorY - 1).length

				lines(cursorY - 1) += lines(cursorY)
				lines.remove(cursorY)
				moveCursor(cx, cursorY - 1)
			}

		case Keyboard.KEY_TAB =>
			val sb = new StringBuilder(lines(cursorY))
			val count = 4 - cursorX % 4

			blinkTick = 0
			showCursor = false
			sb.insert(cursorX, " " * count)
			lines(cursorY) = sb.mkString
			moveCursor(cursorX + count, cursorY)

		case Keyboard.KEY_RETURN | Keyboard.KEY_NUMPADENTER =>
			val line1 = lines(cursorY).take(cursorX)
			val line2 = lines(cursorY).drop(cursorX)
			val indent = if (!line1.isEmpty && line1.last == '{') "    " else ""
			val leading = indent + " " * (line1.length - StringUtils.stripStart(line1, null).length)

			blinkTick = 0
			showCursor = false
			lines(cursorY) = line1
			lines.insert(cursorY + 1, leading + line2)
			moveCursor(leading.length, cursorY + 1)

		case _ => if (font.font.canDisplay(char))
		{
			val sb = new StringBuilder(lines(cursorY))

			blinkTick = 0
			showCursor = false
			sb.insert(cursorX, char)
			lines(cursorY) = sb.mkString
			moveCursor(cursorX + 1, cursorY)
		}
	}

	def mouseWheel(mouseX: Int, mouseY: Int, delta: Int) =
	{
		if (isInside(mouseX, mouseY))
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				scrollH.mouseWheel(mouseX, mouseY, delta)
			else
				scrollV.mouseWheel(mouseX, mouseY, delta)
		}
	}

	def mouseClicked(mouseX: Int, mouseY: Int, button: Int) =
	{
		focused = isInside(mouseX, mouseY)

		if (focused)
		{
			scrollH.mouseClicked(mouseX, mouseY, button)
			scrollV.mouseClicked(mouseX, mouseY, button)

			if (!scrollH.isClicked && !scrollV.isClicked)
			{
				val cw = font.measure(' ')
				val dx = font.measure(lines.length.toString)

				if (mouseY >= y + 4 && mouseX >= x + dx + 6)
				{
					val cx = (mouseX - x - dx - 6) / cw
					val cy = (mouseY - y      - 4) / font.height

					if (cx < clipX && cy < clipY)
						moveCursor(cx + scrollH.pos.toInt, cy + scrollV.pos.toInt)
				}
			}
		}
	}

	def mouseDragged(mouseX: Int, mouseY: Int, button: Int) =
	{
		if (focused)
		{
			scrollH.mouseDragged(mouseX, mouseY, button)
			scrollV.mouseDragged(mouseX, mouseY, button)
		}
	}

	def mouseReleased(mouseX: Int, mouseY: Int, button: Int) =
	{
		if (focused)
		{
			scrollH.mouseReleased(mouseX, mouseY, button)
			scrollV.mouseReleased(mouseX, mouseY, button)
		}
	}
}
