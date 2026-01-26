package me.matin.mcore.managers.hook

import kotlinx.coroutines.*
import me.matin.mcore.managers.plugin.KotlinPlugin
import me.matin.mcore.mcore
import me.matin.mcore.methods.enabled
import kotlin.properties.ReadOnlyProperty

class HooksHandler internal constructor(internal val plugin: KotlinPlugin) {
	
	private val hooks: MutableMap<Hook, Boolean> = mutableMapOf()
	lateinit var scope: CoroutineScope private set
	
	fun observeHook(
		name: String,
		required: Boolean = false,
		onEnable: Hook.StateAction? = null,
		onDisable: Hook.StateAction? = null,
		requirements: Hook.Requirements? = null,
	): ReadOnlyProperty<Any?, Boolean> = mcore.getOrThrow().hooksManager[name, requirements, onEnable, onDisable].let {
		hooks[it] = required
		checkRequired(it)
		ReadOnlyProperty { _, _ -> it.isHooked }
	}
	
	internal fun init() {
		scope = CoroutineScope(plugin.lifecycleScope.coroutineContext + SupervisorJob() + Dispatchers.Default)
	}
	
	internal fun disable() {
		hooks.forEach { (hook) ->
			hook.handlers -= this
			hook.handlers.ifEmpty { mcore.getOrNull()?.hooksManager?.hooks?.remove(hook) }
		}
		scope.cancel(CancellationException("Plugin ${plugin.name} has been disabled."))
	}
	
	internal fun checkRequired(hook: Hook) {
		if (hooks[hook] ?: return) plugin.enabled = false
	}
}