package me.matin.mcore.managers.hook

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.matin.mcore.dispatchers
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal class Hook(val name: String, val requirements: (Plugin) -> Boolean, handler: HooksHandler) {
	
	val stateChanges: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val handlers: MutableSet<HooksHandler> = mutableSetOf(handler)
	
	suspend fun check() = withContext(dispatchers.main) {
		Bukkit.getPluginManager().getPlugin(name)?.takeIf(requirements)?.isEnabled == true
	}.let { stateChanges.value = it }
}
