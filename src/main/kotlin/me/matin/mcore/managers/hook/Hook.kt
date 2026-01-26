package me.matin.mcore.managers.hook

import kotlinx.coroutines.withContext
import me.matin.mcore.managers.plugin.MainBukkitDispatcher
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal class Hook(
	val name: String,
	val requirements: Requirements?,
	handler: HooksHandler,
	onEnable: StateAction?,
	onDisable: StateAction?,
) {
	
	typealias Requirements = (Plugin) -> Boolean
	typealias StateAction = () -> Unit
	
	val handlers = mutableSetOf(handler)
	val enableActions: MutableSet<StateAction> = mutableSetOf()
	val disableActions: MutableSet<StateAction> = mutableSetOf()
	var isHooked: Boolean = check()
		private set
	
	init {
		onEnable?.let {
			if (isHooked) it()
			enableActions += it
		}
		onDisable?.let { disableActions += it }
	}
	
	private fun check() = Bukkit.getPluginManager().getPlugin(name)?.let {
		it.isEnabled && requirements?.invoke(it) != false
	} ?: false
	
	suspend fun check(onEnable: Boolean) = withContext(MainBukkitDispatcher) {
		isHooked = check()
		(if (onEnable) enableActions else disableActions).forEach { action -> action() }
	}
}
