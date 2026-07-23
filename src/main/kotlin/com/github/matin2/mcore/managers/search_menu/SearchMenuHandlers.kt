package com.github.matin2.mcore.managers.search_menu

import com.github.matin2.mcore.managers.PacketManager.sendPacket
import com.github.matin2.mcore.methods.utils.component.plain
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.bukkit.inventory.ItemStack
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketItem

internal data class SearchEntry(val page: Int = 0, val input: String? = null)

internal class SearchCancellationException : Exception()

private typealias SearchEntries = MutableStateFlow<SearchEntry>

internal suspend fun SearchMenu<*>.handleClose(): Unit = coroutineScope {
	launch { if (awaitMenuClose()) throw SearchCancellationException() }
	launch { if (clickEvents.any { it == 34 }) throw SearchCancellationException() }
}

@OptIn(FlowPreview::class)
internal suspend fun SearchMenu<*>.handleInputChange(entries: SearchEntries) =
	inputEvents.debounce(250.milliseconds).collect { input ->
		entries.update { it.copy(page = 0, input = input) }
	}

@OptIn(ExperimentalAtomicApi::class)
internal suspend fun SearchMenu<*>.handlePageChange(entries: SearchEntries, hasNextPage: AtomicBoolean) =
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
context(menu: SearchMenu<T>)
internal suspend fun <T : Any> SearchEntries.handle(hasNextPage: AtomicBoolean): Nothing = collectLatest { entry ->
	if (entry.input == null) {
		val items: List<PacketItem> = menu.items("").toFullWindow(hasNextPage)
		val packet = WrapperPlayServerWindowItems(SEARCH_WINDOW_ID, 0, items, EMPTY)
		return@collectLatest
	}
	menu.items(entry.input).toPageContent(entry, hasNextPage).forEachIndexed { index, item ->
		item ?: return@forEachIndexed
		val packet = WrapperPlayServerSetSlot(SEARCH_WINDOW_ID, 0, index + 3, item)
		menu.owner.sendPacket(packet)
		menu.owner.sendPacket(packet)
	}
}

@OptIn(ExperimentalAtomicApi::class)
context(menu: SearchMenu<T>)
private suspend fun <T : Any> Flow<T>.toFullWindow(hasNextPage: AtomicBoolean) = buildList {
	add(SearchMenuButtons.placeholder)
	repeat(2) { add(PacketItem.EMPTY) }
	var size = 0
	this@toFullWindow.withIndex().take(28).filter {
		if (it.index == 27) {
			hasNextPage.store(true)
			return@filter false
		}
		menu.pageContent[it.index + 3] = it.value
		size++
		true
	}.map { SpigotConversionUtil.fromBukkitItemStack(menu.transform(it.value)) }.toList(this)
	repeat(36 - size) { add(PacketItem.EMPTY) }
	set(34, SearchMenuButtons.close)
	if (hasNextPage.load()) set(38, SearchMenuButtons.pageUp)
}

@OptIn(ExperimentalAtomicApi::class)
context(menu: SearchMenu<T>)
private suspend fun <T : Any> Flow<T>.toPageContent(entry: SearchEntry, hasNextPage: AtomicBoolean) = buildList {
	var size = 0
	var hadNextPage = false
	this@toPageContent.withIndex().drop(entry.page * 27).take(28).filter {
		if (it.index == 27) {
			hadNextPage = true
			return@filter false
		}
		menu.pageContent[it.index + 3] = it.value
		size++
		true
	}.map { SpigotConversionUtil.fromBukkitItemStack(menu.transform(it.value)) }.toList(this)
	hasNextPage.store(hadNextPage)
	repeat(27 - size) {
		add(EMPTY)
		menu.pageContent.remove(it + 3 + size)
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
