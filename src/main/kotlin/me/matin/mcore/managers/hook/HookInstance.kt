package me.matin.mcore.managers.hook

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matin.mcore.mcore
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal data class HookInstance(val name: String, val requirements: (Plugin) -> Boolean) {
	
	@Volatile
	var plugin: Plugin? = null
	val stateChanges: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val initialCheck = Job()
	val handlers: Set<HooksHandler>
		field : MutableSet<HooksHandler> = mutableSetOf()
	private val mutex = Mutex()
	
	constructor(hook: Hook, handler: HooksHandler): this(hook.name, hook.requirements) {
		handlers += handler
		check(true)
		log(handler, true)
	}
	
	suspend operator fun plusAssign(handler: HooksHandler) {
		mutex.withLock { handlers += handler }
		log(handler, true)
	}
	
	suspend operator fun minusAssign(handler: HooksHandler) {
		mutex.withLock { handlers -= handler }
		if (handlers.isEmpty()) mcore.hooksManager -= this
	}
	
	fun check(initial: Boolean) {
		plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf(requirements)
		val enabled = plugin?.isEnabled == true
		stateChanges.value = enabled
		if (initial) initialCheck.complete()
		else handlers.filter { it.logger.enabled }.forEach { log(it, initial) }
	}
	
	private fun log(handler: HooksHandler, initial: Boolean) =
		handler.hooks.find { it.instance == this }?.let { handler.logger.log(it, initial) } ?: Unit
}
