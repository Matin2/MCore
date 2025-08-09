package me.matin.mcore.managers.hook

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

open class Hook(
	val name: String,
	val required: Boolean,
	manager: HooksManager,
	private val requirements: (Plugin) -> Boolean = { true },
): Listener {
	
	private var _plugin: Plugin? = null
	val plugin get() = _plugin
	private val _state = MutableStateFlow(State.NOT_FOUND)
	val state get() = _state.asStateFlow()
	val available get() = _state.value == State.ENABLED
	
	init {
		manager.hooks.add(this)
	}
	
	open fun requirements(plugin: Plugin) = requirements.invoke(plugin)
	open fun onInitialize() {}
	
	internal suspend fun init() {
		_plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf { requirements(it) }
		updateState()
		HookInitialCheckEvent(this).callEvent()
		onInitialize()
	}
	
	internal suspend fun updateState() = _state.emit(_plugin?.run {
		if (isEnabled) State.ENABLED else State.DISABLED
	} ?: State.NOT_FOUND)
	
	enum class State { NOT_FOUND, ENABLED, DISABLED }
}