package me.matin.mcore.managers.hook

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

@Suppress("unused")
@OptIn(ExperimentalCoroutinesApi::class)
open class Hook(
	val name: String,
	val required: Boolean,
	open val requirements: (Plugin) -> Boolean = { true },
): Listener {
	
	val plugin get() = _plugin
	val isAvailable: Boolean get() = availability.value
	val isAvailableAsync: Deferred<Boolean> get() = _isAvailableAsync
	private var _plugin: Plugin? = null
	private val availability: MutableStateFlow<Boolean> = MutableStateFlow(false)
	private var _isAvailableAsync = CompletableDeferred<Boolean>()
	private lateinit var scope: CoroutineScope
	
	fun onStateChange(action: suspend (isAvailable: Boolean) -> Unit): Hook = scope.launch {
		availability.collect { action(it) }
	}.let { this }
	
	protected open suspend fun onStateChange() {}
	protected open suspend fun onInitialStateCheck() {}
	
	internal suspend fun initialize() = coroutineScope {
		scope = this
		onStateChange { onStateChange() }
		check(true)
		onInitialStateCheck()
	}
	
	internal suspend fun check(initial: Boolean) {
		_plugin = Bukkit.getPluginManager().getPlugin(name)?.takeIf { requirements(it) }
		val enabled = _plugin?.isEnabled == true
		if (initial) {
			_isAvailableAsync.complete(enabled)
			HookInitialStateCheckEvent(this).callEvent()
			return
		}
		if (availability.value == enabled) return
		availability.emit(enabled)
		_isAvailableAsync = CompletableDeferred<Boolean>().apply { complete(enabled) }
		HookStateChangeEvent(this).callEvent()
	}
}