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
	
	protected open suspend fun onStateChange() {}
	protected open suspend fun onInitialStateCheck() {}
	
	internal suspend fun initialize() = coroutineScope {
		launch {
			_available.collect {
				HookStateChangeEvent(this@Hook).callEvent()
				onStateChange()
			}
		}
		check()
		HookInitialStateCheckEvent(this@Hook).callEvent()
		onInitialStateCheck()
	}
	
	internal suspend fun check() {
		_plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf { requirements(it) }
		val enabled = _plugin?.isEnabled == true
		if (available == enabled) return
		_available.emit(enabled)
	}
}