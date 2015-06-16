package org.oxygen.redio.gui.components

import java.awt.image.{BufferedImage, DataBufferInt}
import java.awt.{Color, Font, Graphics2D}
import java.nio.ByteBuffer

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU

class TrueTypeFont(val font: Font)
{
	private class TexCoords(val x: Int, val y: Int, val width: Int, val height: Int) {}

	final val TextureWidth = 512
	final val TextureHeight = 512

	private val fontMap = Array.fill[TexCoords](256)(null)

	/* font metrics */
	private val sizeImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
	private val sizeCanvas = sizeImage.getGraphics.asInstanceOf[Graphics2D]

	/* atlas sprite */
	private val atlasImage = new BufferedImage(TextureWidth, TextureHeight, BufferedImage.TYPE_INT_ARGB)
	private val atlasCanvas = atlasImage.getGraphics.asInstanceOf[Graphics2D]

	sizeCanvas.setFont(font)
	atlasCanvas.setColor(new Color(0, 0, 0, 1))
	atlasCanvas.fillRect(0, 0, TextureWidth, TextureHeight)

	/* atlas assembler */
	private val metrics = sizeCanvas.getFontMetrics
	private var texCoordX = 0
	private var texCoordY = 0

	/* render alphabets into atlas image */
	for (ch <- 0 to 255)
	{
		val char = ch.toChar
		val width = measure(char)
		val image = new BufferedImage(width + 2, height + 2, BufferedImage.TYPE_INT_ARGB)
		val canvas = image.getGraphics.asInstanceOf[Graphics2D]

		canvas.setFont(font)
		canvas.setColor(Color.WHITE)
		canvas.drawString(char.toString, 1, ascent + 1)

		if (texCoordX + image.getHeight >= TextureWidth)
		{
			texCoordX = 0
			texCoordY += image.getHeight
		}

		val x = texCoordX
		val y = texCoordY

		texCoordX += image.getWidth
		fontMap(ch) = new TexCoords(x, y, width, height)

		atlasCanvas.drawImage(image, x, y, null)
	}

	/* adjust byte order */
	private val data = atlasImage.getData.getDataBuffer.asInstanceOf[DataBufferInt].getData
	private val buffer = Array.fill[Byte](data.length * 4)(0)
	private val pixels = ByteBuffer.allocateDirect(TextureWidth * TextureHeight * 4)
	private var texture = -1

	for ((pixel, i) <- data.zipWithIndex)
	{
		buffer(i * 4 + 0) = ((pixel & 0x00FF0000) >> 16).toByte
		buffer(i * 4 + 1) = ((pixel & 0x0000FF00) >>  8).toByte
		buffer(i * 4 + 2) = ((pixel & 0x000000FF) >>  0).toByte
		buffer(i * 4 + 3) = ((pixel & 0xFF000000) >> 24).toByte
	}

	def ascent = metrics.getAscent
	def height = metrics.getHeight

	def measure(char: Char): Int =
	{
		val width = metrics.charWidth(char)
		if (width <= 0) metrics.charWidth(' ') else width
	}

	def measure(string: String): Int = string.isEmpty match
	{
		case true  => 0
		case false => string.map(measure).sum
	}

	def drawString(string: String, x: Int, y: Int, color: Color) =
	{
		GlStateManager.bindTexture(texture)
		GL11.glBegin(GL11.GL_QUADS)
		GL11.glColor3f(color.getRed / 256.0f, color.getGreen / 256.0f, color.getBlue / 256.0f)

		var xCoord = x
		for (ch <- string)
		{
			val drawX = xCoord
			val texCoord = fontMap(ch)

			xCoord += texCoord.width
			drawCharTexture(drawX, y, drawX + texCoord.width, y + texCoord.height,
				texCoord.x + 1, texCoord.y + 1, texCoord.x + texCoord.width + 1, texCoord.y + texCoord.height + 1)
		}

		GL11.glEnd()
		xCoord
	}

	def buildTexture() =
	{
		pixels.clear()
		texture = GlStateManager.generateTexture()

		GlStateManager.bindTexture(texture)
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP)
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP)
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)

		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
		GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, TextureWidth, TextureHeight,
			GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels.put(buffer).flip().asInstanceOf[ByteBuffer])
	}

	private def drawCharTexture(x1: Float, y1: Float, x2: Float, y2: Float, u1: Float, v1: Float, u2: Float, v2: Float) =
	{
		GL11.glTexCoord2f(u1 / TextureWidth, v1 / TextureHeight); GL11.glVertex2f(x1, y1)
		GL11.glTexCoord2f(u1 / TextureWidth, v2 / TextureHeight); GL11.glVertex2f(x1, y2)
		GL11.glTexCoord2f(u2 / TextureWidth, v2 / TextureHeight); GL11.glVertex2f(x2, y2)
		GL11.glTexCoord2f(u2 / TextureWidth, v1 / TextureHeight); GL11.glVertex2f(x2, y1)
	}
}
