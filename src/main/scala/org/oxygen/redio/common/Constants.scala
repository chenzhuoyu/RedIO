package org.oxygen.redio.common

import net.minecraft.block.material.{MapColor, Material}

object Constants
{
	final val MOD_ID		= "redio"
	final val MOD_VER		= "v0.1a"
	final val MOD_NAME		= "RedStone IO"

	final val CHANNEL_NAME	= "RedIO"

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
	}
}
