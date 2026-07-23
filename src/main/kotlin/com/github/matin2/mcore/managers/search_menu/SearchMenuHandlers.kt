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
	clickEvents.filter { it == 30 || (it == 38 && hasNextPage.load()) }.collect { slot ->
		entries.update {
			val page = when (slot) {
				38 -> it.page + 1
				30 if it.page > 0 -> it.page - 1
				else -> return@collect
			}
			it.copy(page = page)
		}
	}

@OptIn(ExperimentalAtomicApi::class)
context(menu: SearchMenu<T>)
internal suspend fun <T : Any> SearchEntries.handle(hasNextPage: AtomicBoolean): Nothing = collectLatest { entry ->
	val input = if (entry.input == null) {
		val initialContent = List(39) {
			when (it) {
				0 -> searchItem
				34 -> searchCloseItem
				else -> EMPTY
			}
		}
		val packet = WrapperPlayServerWindowItems(SEARCH_WINDOW_ID, 0, initialContent, EMPTY)
		menu.owner.sendPacket(packet)
		""
	} else entry.input
	menu.items(input).currentPage(entry.page, hasNextPage).collect { [slot, item] ->
		val packet = WrapperPlayServerSetSlot(SEARCH_WINDOW_ID, 0, slot, item)
		menu.owner.sendPacket(packet)
	}
}

@OptIn(ExperimentalAtomicApi::class)
context(menu: SearchMenu<T>)
private fun <T : Any> Flow<T>.currentPage(page: Int, hasNextPage: AtomicBoolean) = flow {
	var size = 0
	var hadNextPage = false
	emit(30 to if (page > 0) prevSearchPageItem else EMPTY)
	withIndex().drop(page * 27).take(28).collect {
		if (it.index == 27) {
			hadNextPage = true
			return@collect
		}
		val slot = it.index + 3
		menu.pageContent[slot] = it.value
		emit(slot to SpigotConversionUtil.fromBukkitItemStack(menu.transform(it.value)))
		size++
	}
	hasNextPage.store(hadNextPage)
	emit(38 to if (hadNextPage) nextSearchPageItem else EMPTY)
	repeat(27 - size) {
		val slot = it + 3 + size
		emit(slot to EMPTY)
		menu.pageContent.remove(slot)
	}
}

@Suppress("NOTHING_TO_INLINE")
inline infix fun ItemStack.matches(input: String): Boolean {
	val name = effectiveName().plain()
	val nameParts = name.splitToSequence(' ')
	return input.splitToSequence(' ').any { inputPart ->
		nameParts.any { it.equals(inputPart, ignoreCase = true) }
	} || name.startsWith(input, ignoreCase = true)
}
