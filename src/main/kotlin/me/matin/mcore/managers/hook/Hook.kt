package me.matin.mcore.managers.hook

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

open class Hook(
	val name: String,
	val required: Boolean,
	manager: HooksManager,
	open val requirements: (Plugin) -> Boolean = { true },
): Listener {
	
	private var _plugin: Plugin? = null
	val plugin get() = _plugin
	private val _available = MutableStateFlow(false)
	val available get() = _available.asStateFlow()
	
	init {
		manager.hooks.add(this)
	}
	
	open suspend fun CoroutineScope.init() {}
	
	context(scope: CoroutineScope)
	internal fun initialize() = scope.launch {
		_plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf { requirements(it) }
		check(true)
		launch { init() }
	}
	
	internal suspend fun check(initial: Boolean) {
		val enabled = _plugin?.isEnabled == true
		if (available.value == enabled) return
		_available.emit(enabled)
		val event = if (initial) HookInitialCheckEvent(this) else HookCheckEvent(this)
		event.callEvent()
	}
}