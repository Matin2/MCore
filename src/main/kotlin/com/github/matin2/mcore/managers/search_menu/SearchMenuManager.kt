package com.github.matin2.mcore.managers.search_menu

import com.github.matin2.mcore.MCore
import com.github.matin2.mcore.managers.PacketManager.sendPacket
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import org.bukkit.entity.Player
import java.util.*

@Suppress("NOTHING_TO_INLINE")
internal class SearchMenuManager(val mcore: MCore) : PacketListenerAbstract(NORMAL) {
	
	val clickEvents = MutableSharedFlow<PacketData<Int>>(extraBufferCapacity = 5, onBufferOverflow = DROP_OLDEST)
	val inputEvents = MutableSharedFlow<PacketData<String>>(extraBufferCapacity = 10, onBufferOverflow = DROP_OLDEST)
	val closeEvents = MutableSharedFlow<UUID>(extraBufferCapacity = 5, onBufferOverflow = DROP_OLDEST)
	
	private var packetEvents = mcore.hooks.packetEvents ?: error("PacketEvents is not available!")
	
	init {
		packetEvents.eventManager.registerListener(this)
	}
	
	override fun onPacketReceive(event: PacketReceiveEvent) {
		val player = event.getPlayer() as? Player ?: return
		val playerId = player.uniqueId
		
		when (event.packetType) {
			PacketType.Play.Client.NAME_ITEM ->
				inputEvents.tryEmit(PacketData(playerId, WrapperPlayClientNameItem(event).itemName ?: return))
			
			PacketType.Play.Client.CLICK_WINDOW -> {
				val packet = WrapperPlayClientClickWindow(event)
				if (packet.windowId != SEARCH_WINDOW_ID) return
				event.isCancelled = true
				val menu = SearchMenu.menus[playerId] ?: return
				when (packet.windowClickType) {
					PICKUP if packet.slot != -999 -> {
						player.setSlot(packet.slot, menu)
						player.emptyCursor()
					}
					
					PICKUP_ALL -> {
						packet.hashedSlots.keys.forEach {
							player.setSlot(it, menu)
						}
						player.emptyCursor()
					}
					
					QUICK_MOVE -> packet.hashedSlots.keys.forEach {
						player.setSlot(it, menu)
					}
					
					CLONE -> player.emptyCursor()
					else -> Unit
				}
				clickEvents.tryEmit(PacketData(playerId, packet.slot))
			}
			
			PacketType.Play.Client.CLOSE_WINDOW -> {
				val packet = WrapperPlayClientCloseWindow(event)
				if (packet.windowId != SEARCH_WINDOW_ID) return
				player.updateInventory()
				closeEvents.tryEmit(playerId)
			}
		}
	}
	
	private inline fun <T : Any> Player.setSlot(slot: Int, menu: SearchMenu<T>) {
		val item = when (slot) {
			0 -> searchItem
			34 -> searchCloseItem
			1, in 30..38 -> EMPTY
			
			2 -> {
				sendPacket(WrapperPlayServerSetSlot(SEARCH_WINDOW_ID, 0, 0, searchItem))
				EMPTY
			}
			
			else -> menu.pageContent[slot]?.let {
				SpigotConversionUtil.fromBukkitItemStack(menu.transform(it))
			} ?: EMPTY
		}
		sendPacket(WrapperPlayServerSetSlot(SEARCH_WINDOW_ID, 0, slot, item))
	}
	
	
	private inline fun Player.emptyCursor() = sendPacket(WrapperPlayServerSetCursorItem(ItemStack.EMPTY))
	
	data class PacketData<T>(val playerId: UUID, val data: T)
}
