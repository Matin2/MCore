package me.matin.mcore.managers.hook

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal data class HookInstance(val name: String, val requirements: (Plugin) -> Boolean) {
	
	var plugin: Plugin? = null
	val stateChanges: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val initialCheck = Job()
	val managers: Set<HooksManager>
		field: MutableSet<HooksManager> = mutableSetOf()
	private val mutex = Mutex()
	
	constructor(hook: Hook, manager: HooksManager): this(hook.name, hook.requirements) {
		managers.add(manager)
	}
	
	suspend fun addManager(manager: HooksManager) = mutex.withLock { managers += manager }
	
	suspend fun removeManager(manager: HooksManager) = mutex.withLock { managers -= manager }
	
	fun check(initial: Boolean) {
		plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf { requirements(it) }
		val enabled = plugin?.isEnabled == true
		if (stateChanges.value == enabled) return
		stateChanges.value = enabled
		if (initial) initialCheck.complete()
	}
}
