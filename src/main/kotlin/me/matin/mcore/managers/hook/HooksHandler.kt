package me.matin.mcore.managers.hook

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import me.matin.mcore.managers.plugin.MainBukkitDispatcher
import me.matin.mcore.mcore
import me.matin.mcore.methods.enabled
import org.bukkit.plugin.Plugin

class HooksHandler internal constructor(internal val plugin: Plugin) {
	
	private val hooks: MutableMap<Hook, Boolean> = mutableMapOf()
	lateinit var scope: CoroutineScope private set
	
	fun observeHook(
		name: String,
		required: Boolean = false,
		requirements: (Plugin) -> Boolean = { true },
		modifyFlow: Flow<Boolean>.() -> Flow<Boolean> = { this },
	) = mcore.hooksManager[name, requirements].let {
		hooks[it] = required
		scope.launch {
			it.stateChanges.filterNotNull().first()
			checkRequired(it)
		}
		HookStateFlow(it, modifyFlow)
	}
	
	internal fun init() {
		scope = CoroutineScope(mcore.lifecycleScope.coroutineContext + SupervisorJob() + Dispatchers.Default)
	}
	
	internal fun disable() {
		hooks.keys.forEach {
			it.handlers -= this
			it.handlers.ifEmpty { mcore.hooksManager.hooks -= it }
		}
		scope.cancel(CancellationException("Plugin ${plugin.name} has been disabled."))
	}
	
	internal suspend fun checkRequired(hook: Hook) {
		if (hooks[hook] ?: return) withContext(MainBukkitDispatcher) { plugin.enabled = false }
	}
}