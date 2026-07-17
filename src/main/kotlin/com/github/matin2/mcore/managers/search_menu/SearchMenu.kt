package com.github.matin2.mcore.managers.search_menu

import com.github.matin2.mcore.MCore
import com.github.matin2.mcore.managers.plugin.koinOf
import com.github.matin2.mcore.methods.utils.component.component
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

const val SEARCH_WINDOW_ID = 451

typealias IndexedItem = IndexedValue<ItemStack>

@Suppress("unused")
suspend fun Player.openSearch(items: List<ItemStack>): IndexedItem? {
	val menu = SearchMenu(this, items)
	return try {
		menu.search()
	} catch (_: SearchCancellationException) {
		null
	} finally {
		menu.close()
	}
}

internal class SearchMenu(val owner: Player, val items: List<ItemStack>) : KoinComponent {
	
	val packetEvents = get<MCore>().hooks.packetEvents ?: error("PacketEvents is missing!")
	
	val pageContent = ConcurrentHashMap<Int, IndexedItem>(27)
	
	private val manager: SearchMenuManager by inject()
	val clickEvents = manager.clickEvents
		.filter { it.playerId == owner.uniqueId.toKotlinUuid() }.map { it.slot }
	val inputEvents = manager.inputEvents
		.filter { it.playerId == owner.uniqueId.toKotlinUuid() }.map { it.input }
	
	
	@OptIn(ExperimentalAtomicApi::class)
	suspend fun search() = coroutineScope {
		menus[owner.uniqueId.toKotlinUuid()] = this@SearchMenu
		val handlers = launch(Dispatchers.IO) {
			val entries = MutableStateFlow(SearchEntry())
			val hasNextPage = AtomicBoolean(false)
			launch { handleClose() }
			launch { handleInputChange(entries) }
			launch { handlePageChange(entries, hasNextPage) }
			launch { open(); entries.handle(hasNextPage) }
		}
		clickEvents
			.filter { it in 3..29 }
			.mapNotNull(pageContent::get)
			.onCompletion { handlers.cancel() }
			.first()
	}
	
	suspend fun awaitMenuClose() = manager.closeEvents.any { it == owner.uniqueId.toKotlinUuid() }
	
	private fun open() {
		val packet = WrapperPlayServerOpenWindow(SEARCH_WINDOW_ID, 8, component("Search"))
		packetEvents.playerManager.sendPacket(owner, packet)
	}
	
	fun close() {
		val packet = WrapperPlayServerCloseWindow(SEARCH_WINDOW_ID)
		packetEvents.playerManager.sendPacket(owner, packet)
		owner.updateInventory()
		menus.remove(owner.uniqueId.toKotlinUuid())
	}
	
	override fun getKoin() = koinOf<MCore>()
	
	companion {
		val menus = ConcurrentHashMap<Uuid, SearchMenu>()
	}
}
