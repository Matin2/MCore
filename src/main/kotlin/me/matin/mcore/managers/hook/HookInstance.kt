package me.matin.mcore.managers.hook

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal data class HookInstance(val name: String, val requirements: (Plugin) -> Boolean) {
	
	var plugin: Plugin? = null
	val stateChanges: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val initialCheck = Job()
	val handlers: Set<HooksHandler>
		field : MutableSet<HooksHandler> = mutableSetOf()
	private val mutex = Mutex()
	
	constructor(hook: Hook): this(hook.name, hook.requirements)
	
	suspend operator fun plusAssign(handler: HooksHandler) = mutex.withLock { handlers += handler }
	
	suspend operator fun minusAssign(handler: HooksHandler) {
		mutex.withLock { handlers -= handler }
		if (handlers.isEmpty()) HooksManager -= this
	}
	
	fun check(initial: Boolean) {
		plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf { requirements(it) }
		val enabled = plugin?.isEnabled == true
		stateChanges.update {
			if (it == enabled) return
			if (initial) initialCheck.complete()
			enabled
		}
		handlers.forEach { log(it, initial) }
	}
	
	internal fun log(handler: HooksHandler, initial: Boolean) =
		handler.hooks.filter { it.instance == this }.forEach { handler.logger.log(it, initial) }
}
