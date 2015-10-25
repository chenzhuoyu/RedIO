package org.oxygen.redio.tileentities

import net.minecraft.block.state.IBlockState
import net.minecraft.command.CommandResultStats.Type
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.network.{NetworkManager, Packet}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util._
import net.minecraft.world.World
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.relauncher.Side
import org.oxygen.redio.RedIO
import org.oxygen.redio.common.Constants
import org.oxygen.redio.items.ItemMemory
import org.oxygen.redio.runtime.ScriptEngine.Executor
import org.oxygen.redio.runtime._
import org.oxygen.redscript.Engine
import org.oxygen.redscript.objects.RedObject

class TileEntityProcessor extends TileEntity with Executor with ICommandSender
{
	var heat: Long = 0
	var name: String = ""
	var script: String = ""
	var inside: ItemStack = null

	val context: Context = new Context(this)
	var onMessage: RedObject = null
    var onTrigger: RedObject = null

	def rename(newName: String) =
	{
		if (!worldObj.isRemote)
		{
			name = newName
			markDirty()
		}
	}

    def trigger(state: Boolean) =
    {
        if (onTrigger != null) execute
        {
            try
            {
                onTrigger(state) match
                {
                    case x: RedObject => x.unref()
                    case _ =>
                }
            } catch
                {
                    case e: RuntimeException =>
                        ejectMemory()
                        RedIO.logger.error("Exception in processor \"" + name + "\", memory ejected.\n" + e.getMessage)
                }
        } match
        {
            case () =>
            case null =>
                ejectMemory()
                RedIO.logger.error("Execution timeout in processor \"" + name + "\", memory ejected.")
        }
    }

	def ejectMemory() =
	{
		if (inside != null)
		{
			if (!worldObj.isRemote)
			{
                if (onMessage != null) onMessage.unref()
				if (onTrigger != null) onTrigger.unref()

				val dx = worldObj.rand.nextDouble() * 0.7 + 0.15
				val dy = worldObj.rand.nextDouble() * 0.7 + 0.66
				val dz = worldObj.rand.nextDouble() * 0.7 + 0.15
				val entity = new EntityItem(worldObj, pos.getX + dx, pos.getY + dy, pos.getZ + dz, inside)

				entity.setDefaultPickupDelay()
				worldObj.spawnEntityInWorld(entity)
			}

			script = ""
			inside = null
			onMessage = null
            onTrigger = null
		}
	}

	def insertMemory(memory: ItemStack) =
	{
		if (memory.hasTagCompound && (memory.getItem == ItemMemory))
		{
			inside = memory.copy()
			script = memory.getTagCompound.getString("script")

			if (!worldObj.isRemote)
			{
				markDirty()
				loadScript(script)
				worldObj.markBlockForUpdate(pos)
			}
		}
	}

	def dispatchPacket(source: String, packet: PacketType): Any = packet match
	{
		case Input => onMessage(source, null)
		case Output(payload) => onMessage(source, payload)
	}

	private def getCallback(mod: RedObject, name: String) =
	{
		try
		{
			mod.getAttrib(name) match
			{
				case f: RedObject => f.isCallable match
				{
					case true => f
					case false =>
                        f.unref()
                        null
				}

				case _ => null
			}
		} catch
		{
			case _: RuntimeException => null
		}
	}

	private def loadScript(script: String) = execute
	{
		try
		{
			val code = Engine.compile(script)
			val module = code.eval

			if (onMessage != null) onMessage.unref()
            if (onTrigger != null) onTrigger.unref()

			module.setAttrib("Context", context)
			onMessage = getCallback(module, "onMessage")
            onTrigger = getCallback(module, "onTrigger")

			code.unref()
			module.unref()
		} catch
		{
			case e: RuntimeException =>
				ejectMemory()
				RedIO.logger.error("Exception in processor \"" + name + "\", memory ejected.\n" + e.getMessage)
		}
	} match
    {
        case () =>
        case null =>
            ejectMemory()
            RedIO.logger.error("Execution timeout in processor \"" + name + "\", memory ejected.")
    }

    override def getName: String = name
    override def getPosition: BlockPos = pos
    override def getEntityWorld: World = worldObj
    override def getDisplayName: IChatComponent = new ChatComponentText(name)
    override def getPositionVector: Vec3 = new Vec3(pos.getX + 0.5d, pos.getY + 0.5d, pos.getZ + 0.5d)
    override def getCommandSenderEntity: Entity = null

    override def canUseCommand(permLevel: Int, commandName: String): Boolean = true
    override def addChatMessage(message: IChatComponent): Unit = ()
    override def setCommandStat(`type`: Type, amount: Int): Unit = ()
    override def sendCommandFeedback(): Boolean = false

	override def writeToNBT(nbt: NBTTagCompound) =
	{
		super.writeToNBT(nbt)
		nbt.setLong("heat", heat)
		nbt.setString("name", name)
		nbt.setString("script", script)

		if (inside != null)
		{
			val memory = new NBTTagCompound

			inside.writeToNBT(memory)
			nbt.setTag("inside", memory)
		}
	}

	override def readFromNBT(nbt: NBTTagCompound) =
	{
		super.readFromNBT(nbt)
		heat = nbt.getLong("heat")
		name = nbt.getString("name")
		script = nbt.getString("script")
		inside = if (nbt.hasKey("inside")) ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inside")) else null

		if (FMLCommonHandler.instance().getEffectiveSide == Side.SERVER)
			loadScript(script)
	}

	override def onDataPacket(net: NetworkManager, packet: S35PacketUpdateTileEntity) =
	{
		readFromNBT(packet.getNbtCompound)
		worldObj.setBlockState(pos, getBlockType.getStateFromMeta(packet.getTileEntityType), 2)
	}

	override def shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newSate: IBlockState) =
		oldState.getBlock != newSate.getBlock

	override def receiveClientEvent(eventId: Int, eventParam: Int): Boolean = eventId match
	{
		case Constants.Events.SMOKE =>
			val x = pos.getX + worldObj.rand.nextDouble()
			val z = pos.getZ + worldObj.rand.nextDouble()
			worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, pos.getY, z, 0.0, 0.05, 0.0)
			true

		case _ => false
	}

	override def getDescriptionPacket: Packet =
	{
		val nbt = new NBTTagCompound

		writeToNBT(nbt)
		new S35PacketUpdateTileEntity(pos, getBlockMetadata, nbt)
	}
}
