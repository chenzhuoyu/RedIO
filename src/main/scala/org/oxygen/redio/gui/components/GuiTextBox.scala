package org.oxygen.redio.gui.components

import java.awt.Color

import net.minecraft.client.gui.{Gui, GuiScreen}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.apache.commons.lang3.StringUtils
import org.lwjgl.input.Keyboard
import org.oxygen.redio.MonoFontRenderer
import org.oxygen.redio.common.Utils

import scala.collection.mutable

@SideOnly(Side.CLIENT)
class GuiTextBox(val id: Int, val x: Int, val y: Int, val width: Int, val height: Int) extends Gui
{
	val lines = mutable.ListBuffer[String]("")

	private var endX = -1
	private var endY = -1
	private var startX = 0
	private var startY = 0

	private var _clipX = 0
	private var _clipY = 0
	private var _cursorX = 0
	private var _cursorY = 0

	private var focused = true
	private var blinkTick = 0L
	private var showCursor = false

	private val font = MonoFontRenderer.font
	private val scrollH = new GuiScrollBar(x, y + height - 8, width, 8, true)
	private val scrollV = new GuiScrollBar(x + width - 8, y, 8, height - 8, false)

	private def isInside(mouseX: Int, mouseY: Int) =
		mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height

	private def isSelected(cx: Int, cy: Int) = endX >= 0 && endY >= 0 &&
	{
		val (x1, y1, x2, y2) = selectedArea
		cx + cy * scrollH.max < x2 + y2 * scrollH.max &&
		x1 + y1 * scrollH.max <= cx + cy * scrollH.max
	}

	private def updateScroll(updatePos: Boolean) =
	{
		scrollV.max = Math.max(_clipY, lines.length)
		scrollH.max = Math.max(_clipX, lines.map(_.length).max + 1)
		scrollH.value = _clipX
		scrollV.value = _clipY

		if (updatePos)
		{
			scrollH.pos = Math.min(Math.min(Math.max(Math.max(scrollH.pos, 0), _cursorX - _clipX + 1), scrollH.max - _clipX), _cursorX)
			scrollV.pos = Math.min(Math.min(Math.max(Math.max(scrollV.pos, 0), _cursorY - _clipY + 1), scrollV.max - _clipY), _cursorY)
		}
	}

	private def moveCursorByKey(x: Int, y: Int, scroll: Boolean): Boolean =
	{
		moveCursor(x, y, scroll)

		if (!GuiScreen.isShiftKeyDown)
			return false

		endX = _cursorX
		endY = _cursorY
		true
	}

	private def replaceSelectionWith(string: String) =
	{
		var x1 = 0
		var x2 = 0
		var y1 = 0
		var y2 = 0

		if (endX < 0 || endY < 0)
		{
			x1 = _cursorX
			x2 = _cursorX
			y1 = _cursorY
			y2 = _cursorY
		}
		else
		{
			val (a, b, c, d) = selectedArea

			x1 = a
			y1 = b
			x2 = c
			y2 = d
		}

		val prev = lines.take(y1)
		val next = lines.drop(y2 + 1)
		val tail = lines(y2).substring(x2)
		val head = lines(y1).substring(0, x1)

		var dx = 0
		var dy = 0
		val text = string.replace("\t", "    ")
		val addLines = text.split("\n")
		val newLines = mutable.ListBuffer[String]()

		if (addLines.length == 1)
		{
			dx = text.length
			newLines.appendAll(prev)
			newLines.append(head + text + tail)
			newLines.appendAll(next)
		}
		else
		{
			val last = addLines.last
			val first = addLines.head

			dx = last.length - x1
			dy = addLines.length - 1

			newLines.appendAll(prev)
			newLines.append(head + first)
			newLines.appendAll(addLines.slice(1, addLines.length - 1))
			newLines.append(last + tail)
			newLines.appendAll(next)
		}

		endX = -1
		endY = -1
		lines.clear()
		lines.appendAll(newLines)
		moveCursor(x1 + dx, y1 + dy, scroll = true)
	}

	def text = lines.mkString("\n")
	def text_=(text: String) =
	{
		lines.clear()
		lines.appendAll(text.split("\n"))
	}

	def clipX = _clipX
	def clipY = _clipY
	def cursorX = _cursorX
	def cursorY = _cursorY
	def scrollTop = scrollV.pos.toInt
	def scrollLeft = scrollH.pos.toInt

	def selectedArea =
	{
		var x2 = endX
		var y2 = endY
		var x1 = startX
		var y1 = startY

		if (y1 > y2 || (y1 == y2 && x1 > x2))
		{
			val tx = x1; x1 = x2; x2 = tx
			val ty = y1; y1 = y2; y2 = ty
		}

		(x1, y1, x2, y2)
	}

	def selectedText: String =
	{
		val (x1, y1, x2, y2) = selectedArea

		if (y1 == y2)
			return lines(y1).substring(x1, x2)

		val intr = lines.slice(y1 + 1, y2)
		val head = lines(y1).substring(x1)
		val tail = lines(y2).substring(0, x2)
		val text = mutable.ListBuffer[String]()

		text.append(head)
		text.appendAll(intr)
		text.append(tail)
		text.mkString("\n")
	}

	def moveCursor(x: Int, y: Int, scroll: Boolean) =
	{
		blinkTick = 0
		showCursor = false

		synchronized
		{
			_cursorY = Math.min(Math.max(y, 0), lines.length - 1)
			_cursorX = Math.min(Math.max(x, 0), lines(_cursorY).length)
			updateScroll(scroll)
		}
	}

	def drawTextBox() =
	{
		val cw = font.measure(' ')
		val dx = font.measure(lines.length.toString)

		_clipX = (width - dx - 16) / cw
		_clipY = (height - 8) / font.height

		synchronized
		{
			updateScroll(false)
		}

		Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0x80000000)
		Utils.strokeRect(x, y, width, height, 0xffa0a0a0)
		drawVerticalLine(x + dx + 4, y, y + height - 8, 0xff303030)

		scrollH.drawScrollBar()
		scrollV.drawScrollBar()

		for ((line, i) <- lines.slice(scrollV.pos.toInt, scrollV.pos.toInt + _clipY).zipWithIndex)
		{
			val lineno = (scrollV.pos.toInt + i + 1).toString
			val string = if (scrollH.pos.toInt >= line.length) "" else
				line.substring(scrollH.pos.toInt, Math.min(scrollH.pos.toInt + _clipX, line.length))

			var cx = scrollH.pos.toInt
			val cy = scrollV.pos.toInt + i

			var drawX = x + dx + 6
			val drawY = y + i * font.height + 4

			font.drawStart()
			font.drawColor(Color.white)

			string.foreach((char: Char) =>
			{
				if (isSelected(cx, cy))
				{
					font.drawRect(char, drawX, drawY, Color.blue)
					font.drawColor(Color.white)
				}

				cx += 1
				drawX = font.drawChar(char, drawX, drawY)
			})

			font.drawEnd()
			font.drawString(lineno, x + dx - font.measure(lineno) + 2, y + i * font.height + 4, Color.darkGray)
		}

		if (System.currentTimeMillis() - blinkTick > 500)
		{
			blinkTick = System.currentTimeMillis()
			showCursor = !showCursor
		}

		if (focused && showCursor)
			drawHorizontalLine(
				x + dx + cw * (_cursorX - scrollH.pos.toInt) + 6,
				x + dx + cw * (_cursorX - scrollH.pos.toInt + 1) + 4,
				y + font.height * (_cursorY - scrollV.pos.toInt + 1) + 2,
				0xffffffff)
	}

	def keyEvent(char: Char, code: Int) = if (focused)
	{
		var selecting = GuiScreen.isCtrlKeyDown

		code match
		{
			case Keyboard.KEY_UP	=> selecting = moveCursorByKey(_cursorX, _cursorY - 1, scroll = true)
			case Keyboard.KEY_DOWN	=> selecting = moveCursorByKey(_cursorX, _cursorY + 1, scroll = true)
			case Keyboard.KEY_LEFT	=> selecting = moveCursorByKey(_cursorX - 1, _cursorY, scroll = true)
			case Keyboard.KEY_RIGHT	=> selecting = moveCursorByKey(_cursorX + 1, _cursorY, scroll = true)

			case Keyboard.KEY_HOME	=> selecting = moveCursorByKey(0, _cursorY, scroll = true)
			case Keyboard.KEY_END	=> selecting = moveCursorByKey(lines(_cursorY).length, _cursorY, scroll = true)

			case Keyboard.KEY_LSHIFT | Keyboard.KEY_RSHIFT =>
				startX = _cursorX
				startY = _cursorY
				selecting = true

			case Keyboard.KEY_PRIOR =>
				selecting = moveCursorByKey(_cursorX, _cursorY - _clipY, scroll = false)
				scrollV.pos = Math.max(scrollV.pos - _clipY, 0)

			case Keyboard.KEY_NEXT =>
				selecting = moveCursorByKey(_cursorX, _cursorY + _clipY, scroll = false)
				scrollV.pos = Math.min(scrollV.pos + _clipY, scrollV.max - _clipY)

			case Keyboard.KEY_BACK => if (endX >= 0 && endY >= 0)
			{
				replaceSelectionWith("")
			}
			else if (_cursorX > 0)
			{
				val sb = new StringBuilder(lines(_cursorY))

				moveCursor(_cursorX - 1, _cursorY, scroll = true)
				sb.deleteCharAt(_cursorX)
				lines(_cursorY) = sb.mkString
			}
			else if (_cursorY > 0)
			{
				val cx = lines(_cursorY - 1).length

				lines(_cursorY - 1) += lines(_cursorY)
				lines.remove(_cursorY)
				moveCursor(cx, _cursorY - 1, scroll = true)
			}

			case Keyboard.KEY_DELETE => if (endX >= 0 && endY >= 0)
			{
				replaceSelectionWith("")
			}
			else if (_cursorX < lines(_cursorY).length)
			{
				val sb = new StringBuilder(lines(_cursorY))

				sb.deleteCharAt(_cursorX)
				lines(_cursorY) = sb.mkString
			}
			else if (_cursorY < lines.length - 1)
			{
				lines(_cursorY) += lines(_cursorY + 1)
				lines.remove(_cursorY + 1)

				synchronized
				{
					updateScroll(true)
				}
			}

			case Keyboard.KEY_TAB =>
				val sb = new StringBuilder(lines(_cursorY))
				val count = 4 - _cursorX % 4

				sb.insert(_cursorX, " " * count)
				lines(_cursorY) = sb.mkString
				moveCursor(_cursorX + count, _cursorY, scroll = true)

			case Keyboard.KEY_RETURN | Keyboard.KEY_NUMPADENTER =>
				val line1 = lines(_cursorY).take(_cursorX)
				val line2 = lines(_cursorY).drop(_cursorX)
				val indent = if (!line1.isEmpty && line1.last == '{') "    " else ""
				val leading = indent + " " * (line1.length - StringUtils.stripStart(line1, null).length)

				lines(_cursorY) = line1
				lines.insert(_cursorY + 1, leading + line2)
				moveCursor(leading.length, _cursorY + 1, scroll = true)

			case Keyboard.KEY_A if GuiScreen.isCtrlKeyDown =>
				endY = lines.length - 1
				endX = lines.last.length
				startX = 0
				startY = 0
				selecting = true

			case Keyboard.KEY_C if GuiScreen.isCtrlKeyDown =>
				GuiScreen.setClipboardString(selectedText)

			case Keyboard.KEY_V if GuiScreen.isCtrlKeyDown =>
				replaceSelectionWith(GuiScreen.getClipboardString)

			case Keyboard.KEY_X if GuiScreen.isCtrlKeyDown =>
				GuiScreen.setClipboardString(selectedText)
				replaceSelectionWith("")

			case _ => if (font.font.canDisplay(char))
			{
				val sb = new StringBuilder(lines(_cursorY))

				sb.insert(_cursorX, char)
				lines(_cursorY) = sb.mkString
				moveCursor(_cursorX + 1, _cursorY, scroll = true)
			}
		}

		blinkTick = 0
		showCursor = false

		if (!selecting)
		{
			endX = -1
			endY = -1
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

					if (cx < _clipX && cy < _clipY)
					{
						moveCursor(cx + scrollH.pos.toInt, cy + scrollV.pos.toInt, scroll = true)

						endX = -1
						endY = -1
						startX = _cursorX
						startY = _cursorY
					}
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

			if (!scrollH.isClicked && !scrollV.isClicked)
			{
				val cw = font.measure(' ')
				val dx = font.measure(lines.length.toString)

				if (mouseY >= y + 4 && mouseX >= x + dx + 6)
				{
					val cx = (mouseX - x - dx - 6) / cw
					val cy = (mouseY - y      - 4) / font.height

					if (cx < _clipX && cy < _clipY)
					{
						moveCursor(cx + scrollH.pos.toInt, cy + scrollV.pos.toInt, scroll = true)

						endX = _cursorX
						endY = _cursorY
					}
				}
			}
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
