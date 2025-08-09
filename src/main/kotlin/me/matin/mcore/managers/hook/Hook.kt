package me.matin.mcore.managers.hook

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
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
	val available get() = _available.value
	
	init {
		manager.hooks.add(this)
	}
	
	protected open suspend fun onInitialCheck() {}
	protected open suspend fun onCheck() {}
	
	internal suspend fun initialize() = coroutineScope {
		launch {
			_available.collect {
				onCheck()
			}
		}
		_plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf { requirements(it) }
		check(true)
		onInitialCheck()
	}
	
	internal suspend fun check(initial: Boolean) {
		val enabled = _plugin?.isEnabled == true
		if (available == enabled) return
		_available.emit(enabled)
		val event = if (initial) HookInitialCheckEvent(this) else HookCheckEvent(this)
		event.callEvent()
	}
}