package org.oxygen.redio.common

import java.util.Properties

import net.minecraft.block.material.{MapColor, Material}

object Constants
{
	final val MOD_ID		= "redio"
	final val MOD_VER		= "v0.1a"
	final val MOD_NAME		= "RedStone IO"

	object Gui
	{
		class GuiChannel
		{
			final val NAME =
				"RedIO|" + getClass.getName.split("\\$").last
		}

		object SetName extends GuiChannel
		{
			final val ID		= 0
			final val BTN_OK	= 0
			final val TEXT_NAME	= 1
		}

		object EditSource extends GuiChannel
		{
			final val ID		= 1
		}
	}

	object Meta
	{
		final val NORMAL		= 0x00
		final val DAMAGED		= 0x08
		final val WORKING		= 0x04
		final val FACING_MASK	= 0x03
	}

	object Events
	{
		final val SMOKE			= 0
	}

	object Materials
	{
		object HeatSink extends Material(MapColor.goldColor)
		{
			setRequiresTool()
			setNoPushMobility()
		}

		object Programmer extends Material(MapColor.lapisColor)
		{
			setRequiresTool()
			setImmovableMobility()
		}
	}

	object NameMapper
	{
		val fields = new Properties()
		val methods = new Properties()

		def init() =
		{
			fields.load(getClass.getResourceAsStream("/fields.properties"))
			methods.load(getClass.getResourceAsStream("/methods.properties"))
		}
	}
}
