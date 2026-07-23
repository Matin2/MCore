package com.github.matin2.mcore.managers.search_menu

import com.github.matin2.mcore.MCore
import com.github.matin2.mcore.managers.PacketManager.sendPacket
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

internal typealias SearchFlowBuilder<T> = (input: String) -> Flow<T>

internal typealias ItemBuilder<T> = (item: T) -> ItemStack

internal typealias PageContent<T> = ConcurrentHashMap<Int, T & Any>

@Suppress("unused")
suspend inline fun Player.openSearch(items: List<ItemStack>) = openSearch({ it.value }) { input ->
	if (input.isBlank()) items.withIndex().asFlow()
	else items.withIndex().filter { it.value matches input }.asFlow()
}

suspend fun <T : Any> Player.openSearch(transform: ItemBuilder<T>, items: SearchFlowBuilder<T>): T? {
	val menu = SearchMenu(this, items, transform)
	return try {
		menu.search()
	} catch (_: SearchCancellationException) {
		null
	} finally {
		menu.close()
	}
}

internal class SearchMenu<T : Any>(
	val owner: Player,
	val items: SearchFlowBuilder<T>,
	val transform: ItemBuilder<T>
) : KoinComponent {
	
	val pageContent = PageContent<T>(27)
	
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
		owner.sendPacket(WrapperPlayServerOpenWindow(SEARCH_WINDOW_ID, 8, component("Search")))
	}
	
	fun close() {
		owner.sendPacket(WrapperPlayServerCloseWindow(SEARCH_WINDOW_ID))
		owner.updateInventory()
		menus.remove(owner.uniqueId.toKotlinUuid())
	}
	
	override fun getKoin() = koinOf<MCore>()
	
	companion {
		val menus = ConcurrentHashMap<Uuid, SearchMenu<*>>()
	}
}
