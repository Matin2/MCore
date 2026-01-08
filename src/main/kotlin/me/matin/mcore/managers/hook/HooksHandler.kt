package me.matin.mcore.managers.hook

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.matin.mcore.dispatchers
import me.matin.mcore.mcore
import me.matin.mcore.methods.enabled
import org.bukkit.plugin.Plugin

typealias Hooked = Boolean

class HooksHandler internal constructor(internal val plugin: Plugin) {
	
	private val hooks: MutableMap<Hook, Boolean> = mutableMapOf()
	lateinit var scope: CoroutineScope private set
	
	fun observeHook(
		name: String,
		required: Boolean = false,
		requirements: (Plugin) -> Boolean = { true },
	): StateFlow<Hooked> = mcore.hooksManager[name, requirements]
		.also {
			hooks[it] = required
			scope.launch {
				it.stateChanges.filterNotNull().first()
				checkRequired(it)
			}
		}.stateChanges
		.filterNotNull()
		.stateIn(scope, SharingStarted.Eagerly, false)
	
	internal fun init() {
		scope = CoroutineScope(mcore.coroutineContext + SupervisorJob() + dispatchers.async)
	}
	
	internal fun disable() {
		hooks.keys.forEach {
			it.handlers -= this
			it.handlers.ifEmpty { mcore.hooksManager.hooks -= it }
		}
		scope.cancel(CancellationException("Plugin ${plugin.name} has been disabled."))
	}
	
	internal suspend fun checkRequired(hook: Hook) {
		if (hooks[hook] ?: return) withContext(dispatchers.main) { plugin.enabled = false }
	}
}