package com.github.matin2.mcore.managers.search_menu

import com.github.matin2.mcore.methods.utils.component.plain
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketItem

internal data class SearchEntry(val page: Int = 0, val input: String? = null)

class SearchCancellationException : Exception()

internal suspend fun SearchMenu.handleClose(): Unit = coroutineScope {
	launch { if (awaitMenuClose()) throw SearchCancellationException() }
	launch { if (clickEvents.any { it == 34 }) throw SearchCancellationException() }
}

@OptIn(FlowPreview::class)
internal suspend fun SearchMenu.handleInputChange(entries: MutableStateFlow<SearchEntry>) =
	inputEvents.debounce(250.milliseconds).collect { input ->
		entries.update { it.copy(page = 0, input = input) }
	}

@OptIn(ExperimentalAtomicApi::class)
internal suspend fun SearchMenu.handlePageChange(
	entries: MutableStateFlow<SearchEntry>,
	hasNextPage: AtomicBoolean
) =
	clickEvents.filter { it == 30 || it == 38 }.collect { slot ->
		entries.update {
			val page = when (slot) {
				38 if hasNextPage.load() -> it.page + 1
				30 if it.page > 0 -> it.page - 1
				else -> it.page
			}
			it.copy(page = page)
		}
	}

@OptIn(ExperimentalAtomicApi::class)
context(menu: SearchMenu)
internal suspend fun MutableStateFlow<SearchEntry>.handle(hasNextPage: AtomicBoolean): Nothing =
	collectLatest { entry ->
		if (entry.input == null) {
			val items: List<PacketItem> = menu.items.asSequence().toFullWindowContent(menu.pageContent, hasNextPage)
			val packet = WrapperPlayServerWindowItems(SEARCH_WINDOW_ID, 0, items, EMPTY)
			menu.packetEvents.playerManager.sendPacket(menu.owner, packet)
			return@collectLatest
		}
		menu.items.asSequence().toPageContent(entry, menu.pageContent, hasNextPage).forEachIndexed { index, item ->
			item ?: return@forEachIndexed
			val packet = WrapperPlayServerSetSlot(SEARCH_WINDOW_ID, 0, index + 3, item)
			menu.packetEvents.playerManager.sendPacket(menu.owner, packet)
		}
	}

@OptIn(ExperimentalAtomicApi::class)
private fun Sequence<ItemStack>.toFullWindowContent(
	pageContent: ConcurrentHashMap<Int, IndexedItem>,
	hasNextPage: AtomicBoolean
) = buildList {
	add(SearchMenuButtons.placeholder)
	repeat(2) { add(PacketItem.EMPTY) }
	var size = 0
	this@toFullWindowContent.take(28).filterIndexed { index, value ->
		if (index == 27) {
			hasNextPage.store(true)
			return@filterIndexed false
		}
		pageContent[index + 3] = IndexedValue(index, value)
		size++
		true
	}.mapTo(this) { SpigotConversionUtil.fromBukkitItemStack(it) }
	repeat(36 - size) { add(PacketItem.EMPTY) }
	set(34, SearchMenuButtons.close)
	if (hasNextPage.load()) set(38, SearchMenuButtons.pageUp)
}

@OptIn(ExperimentalAtomicApi::class)
private fun Sequence<ItemStack>.toPageContent(
	entry: SearchEntry,
	pageContent: ConcurrentHashMap<Int, IndexedItem>,
	hasNextPage: AtomicBoolean
) = buildList {
	var size = 0
	this@toPageContent.withIndex()
		.let { items -> if (entry.input!!.isBlank()) items else items.filter { it.value matches entry.input } }
		.drop(entry.page * 27).take(28).filterIndexed { index, page ->
			if (index == 27) {
				hasNextPage.store(true)
				return@filterIndexed false
			}
			pageContent[index + 3] = page
			size++
			true
		}.mapTo(this) { SpigotConversionUtil.fromBukkitItemStack(it.value) }
	repeat(27 - size) {
		add(EMPTY)
		pageContent.remove(size + 2)
	}
	add(if (entry.page > 0) SearchMenuButtons.pageDown else EMPTY)
	repeat(7) { add(null) }
	add(if (hasNextPage.load()) SearchMenuButtons.pageUp else EMPTY)
}

@Suppress("NOTHING_TO_INLINE")
inline infix fun ItemStack.matches(input: String): Boolean {
	val name = effectiveName().plain()
	val nameParts = name.splitToSequence(' ')
	return input.splitToSequence(' ').any { inputPart ->
		nameParts.any { it.equals(inputPart, ignoreCase = true) }
	} || name.startsWith(input, ignoreCase = true)
}
