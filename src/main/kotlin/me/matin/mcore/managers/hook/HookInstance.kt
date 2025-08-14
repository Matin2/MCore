package me.matin.mcore.managers.hook

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal data class HookInstance(val name: String, val requirements: (Plugin) -> Boolean) {
	
	var plugin: Plugin? = null
	val stateChanges: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val initialCheck = Job()
	
	constructor(hook: Hook): this(hook.name, hook.requirements)
	
	fun check(initial: Boolean) {
		plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf { requirements(it) }
		val enabled = plugin?.isEnabled == true
		if (stateChanges.value == enabled) return
		stateChanges.value = enabled
		if (initial) initialCheck.complete()
	}
}
