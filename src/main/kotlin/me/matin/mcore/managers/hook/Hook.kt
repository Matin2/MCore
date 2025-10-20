package me.matin.mcore.managers.hook

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matin.mcore.MCore
import me.matin.mcore.methods.readOnly
import me.matin.mcore.methods.registerListeners
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

@Suppress("unused")
open class Hook(
	val name: String,
	val required: Boolean,
	open val requirements: (Plugin) -> Boolean = { true },
): Listener {
	
	typealias Hooked = Boolean
	
	val plugin get() = instance.plugin
	val isHooked: Hooked get() = instance.stateChanges.value
	val stateChanges: Flow<Hooked> get() = _stateChanges
	val initialCheck: Job get() = _initialCheck
	private lateinit var _stateChanges: SharedFlow<Hooked>
	private lateinit var _initialCheck: Job
	internal lateinit var instance: HookInstance
	protected open suspend fun onInitialCheck() {}
	
	internal suspend fun init(plugin: Plugin) {
		_stateChanges = instance.stateChanges.asSharedFlow()
		_initialCheck = instance.initialCheck.readOnly
		coroutineScope {
			launch {
				_initialCheck.join()
				launch { onInitialCheck() }
				withContext(MCore.serverDispatcher) { plugin.registerListeners(this@Hook) }
			}
		}
	}
}