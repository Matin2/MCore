package me.matin.mcore.managers.hook

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

typealias Hooked = Boolean

@Suppress("unused")
open class Hook(
	val name: String,
	val required: Boolean,
	open val requirements: (Plugin) -> Boolean = { true },
): Listener {
	
	val plugin get() = _plugin
	val isHooked: Hooked get() = _stateChanges.value
	val stateChanges: SharedFlow<Hooked> get() = _stateChanges.asSharedFlow()
	val initialCheck: Job get() = _initialCheck
	private var _plugin: Plugin? = null
	private val _stateChanges: MutableStateFlow<Boolean> = MutableStateFlow(false)
	private var _initialCheck = Job()
	
	protected open suspend fun onStateChange() {}
	protected open suspend fun onInitialCheck() {}
	
	internal suspend fun initialize() = coroutineScope {
		launch { _stateChanges.collect { onStateChange() } }
		launch { check(true) }
	}
	
	internal suspend fun check(initial: Boolean) {
		_plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf { requirements(it) }
		val enabled = _plugin?.isEnabled == true
		if (initial) {
		 _initialCheck.complete()
		 HookInitialCheckEvent(this).callEvent()
		 onInitialCheck()
			return
		}
		if (_stateChanges.value == enabled) return
		_stateChanges.emit(enabled)
		HookStateChangeEvent(this).callEvent()
	}
}