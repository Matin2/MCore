package me.matin.mcore.managers

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketListenerPriority.NORMAL
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.OPEN_WINDOW
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow
import me.matin.mcore.methods.async
import me.matin.mcore.methods.sync
import net.kyori.adventure.text.Component
import org.bukkit.Material.TOTEM_OF_UNDYING
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration.Companion.milliseconds

@Suppress("unused")
object PacketManager {
	
	/**
	 * Shows totem animation to the selected player.
	 *
	 * @param player Selected player
	 * @param model (Optional) CustomModelData for the totem
	 */
	@JvmStatic
	fun showTotem(player: Player, model: Int = -1) = async { thread ->
		if (model < 0) playTotem(player).also { return@async }
		val oldItem = player.inventory.itemInOffHand
		val item = ItemStack(TOTEM_OF_UNDYING)
		item.editMeta { it.setCustomModelData(model) }
		var isItemSet = false
		setItem(player, item) { isItemSet = true }
		thread.pauseWhile(!isItemSet, 10.milliseconds)
		playTotem(player)
		setItem(player, oldItem)
	}
	
	private fun playTotem(player: Player) = WrapperPlayServerEntityStatus(player.entityId, 35).let {
		PacketEvents.getAPI().playerManager.sendPacket(player, it)
	}
	
	private fun setItem(player: Player, item: ItemStack, also: () -> Unit = {}) = sync {
		player.inventory.setItem(40, item)
		also()
	}
	
	/**
	 * Changes the title of the inventory witch the selected player is
	 * currently viewing.
	 *
	 * @param player Selected player
	 * @param title New title
	 */
	@JvmStatic
	fun changeInvTitle(player: Player, title: Component) {
		val (containerId, type) = InventoryTitle.idType[player.name] ?: return
		val wrapper = WrapperPlayServerOpenWindow(containerId, type, title)
		PacketEvents.getAPI().playerManager.sendPacket(player, wrapper)
	}
}

internal object InventoryTitle: PacketListenerAbstract(NORMAL) {
	
	val idType = mutableMapOf<String, Pair<Int, Int>>()
	
	override fun onPacketSend(event: PacketSendEvent) {
		if (event.packetType != OPEN_WINDOW) return
		val wrapper = WrapperPlayServerOpenWindow(event)
		val player = event.user?.name ?: return
		idType[player] = wrapper.containerId to wrapper.type
	}
}