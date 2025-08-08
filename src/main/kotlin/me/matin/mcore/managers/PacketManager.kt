package me.matin.mcore.managers

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketListenerPriority.NORMAL
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import kotlinx.coroutines.launch
import me.matin.mcore.MCore.Companion.pluginScope
import me.matin.mcore.managers.InventoryTitle.openWindows
import net.kyori.adventure.text.Component
import org.bukkit.Material.TOTEM_OF_UNDYING
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

@Suppress("unused", "UnstableApiUsage")
object PacketManager {
	
	@JvmStatic
	var InventoryView.displayTitle: Component?
		get() = openWindows[player]?.title
		set(value) {
			value ?: return
			pluginScope.launch {
				val user = player as Player
				val wrapper = openWindows[user]?.apply { title = value } ?: return@launch
				PacketEvents.getAPI().playerManager.sendPacket(user, wrapper)
				user.updateInventory()
				openWindows[user] = wrapper
			}
		}
	
	/**
	 * Shows totem animation to the selected player.
	 *
	 * @param model (Optional) CustomModelData for the totem
	 * @receiver Selected player
	 */
	@JvmStatic
	fun Player.showTotem(model: CustomModelData? = null) {
		model ?: run { sendTotemPacket(); return }
		val item = inventory.itemInOffHand
		val totem = ItemStack.of(TOTEM_OF_UNDYING).apply {
			setData(DataComponentTypes.CUSTOM_MODEL_DATA, model)
		}
		inventory.setItem(40, totem)
		sendTotemPacket()
		inventory.setItem(40, item)
	}
	
	private fun Player.sendTotemPacket() =
		PacketEvents.getAPI().playerManager.sendPacket(this, WrapperPlayServerEntityStatus(entityId, 35))
}

internal object InventoryTitle: PacketListenerAbstract(NORMAL) {
	
	val openWindows: MutableMap<Player, WrapperPlayServerOpenWindow> = mutableMapOf()
	
	override fun onPacketSend(event: PacketSendEvent) {
		val player: Player = event.getPlayer() ?: return
		when (event.packetType) {
			Play.Server.OPEN_WINDOW -> openWindows[player] = WrapperPlayServerOpenWindow(event)
			Play.Server.CLOSE_WINDOW -> openWindows.remove(player)
		}
	}
}