package org.oxygen.redio.common

import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.MathHelper

object Utils
{
	def getPlayerFacing(player: EntityLivingBase): Int =
		MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5d) & 0x03
}
