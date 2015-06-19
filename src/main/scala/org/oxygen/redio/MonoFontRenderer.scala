package org.oxygen.redio

import net.minecraft.client.resources.{IResourceManager, IResourceManagerReloadListener}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.oxygen.redio.common.Constants
import org.oxygen.redio.gui.components.TrueTypeFont

@SideOnly(Side.CLIENT)
object MonoFontRenderer extends IResourceManagerReloadListener
{
	final val font = new TrueTypeFont(Constants.Fonts.Monospace.deriveFont(16.0f))
	override def onResourceManagerReload(resourceManager: IResourceManager): Unit =
	{
		RedIO.logger.info("Reloading Monospace Font Renderers ...")
		font.buildTexture()
	}
}
