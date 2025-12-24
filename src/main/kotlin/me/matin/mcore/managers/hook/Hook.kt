package me.matin.mcore.managers.hook

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matin.mcore.dispatchers
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
	val stateChanges: Flow<Hooked> get() = instance.stateChanges.asSharedFlow()
	val initialCheck: Job get() = instance.initialCheck
	
	@Volatile
	internal lateinit var instance: HookInstance
	protected open suspend fun onInitialCheck() {}
	
	context(handler: HooksHandler)
	internal suspend fun init() {
		withContext(dispatchers.main) { handler.plugin.registerListeners(this@Hook) }
		setInstance(handler)
		handler.scope.launch {
			instance.initialCheck.join()
			onInitialCheck()
		}
	}
	
	private suspend fun setInstance(handler: HooksHandler) {
		instance = HooksManager.hookInstances.find {
			it.name == name && it.requirements == requirements
		}?.also { it += handler } ?: HookInstance(this, handler).also { HooksManager += it }
	}
}