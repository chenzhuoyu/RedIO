package org.oxygen.redio.runtime

import net.minecraft.command.{PlayerNotFoundException, PlayerSelector}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.{EnumChatFormatting, ChatStyle, ChatComponentText}
import org.oxygen.redio.tileentities.TileEntityProcessor

import scala.collection.JavaConversions._

class Context(private val processor: TileEntityProcessor)
{
	def recv(source: String): Any =
	{
		PacketNode.dispatch(processor.getWorld, processor.getPos, processor.name, source, Input) match
		{
			case None => null
			case Some(result) => result
		}
	}

	def send(target: String, payload: Any): Any =
	{
		PacketNode.dispatch(processor.getWorld, processor.getPos, processor.name, target, Output(payload)) match
		{
			case None => null
			case Some(result) => result
		}
	}

    def tell(selector: String, message: String): Any = try
    {
        for (player <- PlayerSelector.matchEntities(processor, selector, classOf[EntityPlayerMP]))
            player.asInstanceOf[EntityPlayerMP].addChatComponentMessage(new ChatComponentText(s"@${processor.name}: $message"))
    } catch
    {
        case _:PlayerNotFoundException => null
    }

    def tell(selector: String, color: String, message: String): Any = try
    {
        for (player <- PlayerSelector.matchEntities(processor, selector, classOf[EntityPlayerMP]))
            player.asInstanceOf[EntityPlayerMP].addChatComponentMessage(
                new ChatComponentText(s"@${processor.name}: $message").setChatStyle(
                    (new ChatStyle).setColor(EnumChatFormatting.getValueByName(color.toUpperCase))))
    } catch
    {
        case _:PlayerNotFoundException => null
    }
}
