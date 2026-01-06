package me.matin.mcore.managers.hook

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matin.mcore.dispatchers
import me.matin.mcore.mcore
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

internal data class HookInstance(val name: String, val requirements: (Plugin) -> Boolean) {
	
	@Volatile
	var plugin: Plugin? = null
	val stateChanges: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val handlers: Set<HooksHandler>
		field : MutableSet<HooksHandler> = ConcurrentHashMap.newKeySet()
	
	constructor(hook: Hook, handler: HooksHandler): this(hook.name, hook.requirements) {
		handlers += handler
		mcore.launch { check(true) }
		log(handler, true)
	}
	
	operator fun plusAssign(handler: HooksHandler) {
		handlers += handler
		log(handler, true)
	}
	
	operator fun minusAssign(handler: HooksHandler) {
		handlers -= handler
		if (handlers.isEmpty()) mcore.hooksManager -= this
	}
	
	suspend fun check(initial: Boolean) {
		plugin = withContext(dispatchers.main) { Bukkit.getPluginManager().getPlugin(name)?.takeIf(requirements) }
		stateChanges.value = plugin?.isEnabled == true
		if (!initial) handlers.filter { it.logger.enabled }.forEach { log(it, initial) }
	}
	
	private fun log(handler: HooksHandler, initial: Boolean) =
		handler.hooks.find { it.instance == this }?.let { handler.logger.log(it, initial) } ?: Unit
}
