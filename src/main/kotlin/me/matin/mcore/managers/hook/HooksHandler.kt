package me.matin.mcore.managers.hook

import kotlinx.coroutines.*
import me.matin.mcore.MCore
import me.matin.mcore.methods.enabled
import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin

@Suppress("unused", "NOTHING_TO_INLINE")
class HooksHandler private constructor(internal val plugin: Plugin) {
	
	val hooks: Set<Hook>
		field : MutableSet<Hook> = mutableSetOf()
	lateinit var scope: CoroutineScope private set
	internal var logger = Logger { plugin.componentLogger.info(it) }
		private set
	
	fun configureLogger(config: Logger.() -> Unit) {
		logger = logger.apply(config)
	}
	
	fun register(hook: Hook): Boolean {
		if (hooks.any { it.name == hook.name }) return false
		return hooks.add(hook)
	}
	
	inline fun register(vararg hooks: Hook): Boolean = hooks.all { register(it) }
	inline fun register(hooks: Iterable<Hook>): Boolean = hooks.all { register(it) }
	
	inline operator fun plusAssign(hook: Hook) {
		register(hook)
	}
	
	inline operator fun plusAssign(hooks: Iterable<Hook>) {
		register(hooks)
	}
	
	fun unregister(hook: Hook) = hooks.remove(hook)
	inline fun unregister(vararg hooks: Hook): Boolean = hooks.all { unregister(it) }
	inline fun unregister(hooks: Iterable<Hook>): Boolean = hooks.all { unregister(it) }
	
	inline operator fun minusAssign(hook: Hook) {
		unregister(hook)
	}
	
	inline operator fun minusAssign(hooks: Iterable<Hook>) {
		unregister(hooks)
	}
	
	internal fun onPluginStateChange(onDisable: Boolean) {
		if (onDisable) {
			HooksManager -= this
			scope.cancel(CancellationException("Plugin ${plugin.name} has been disabled."))
			return
		}
		scope = CoroutineScope(HooksManager.scope.coroutineContext + Job(HooksManager.scope.coroutineContext.job))
		scope.launch {
			manageHooks()
			checkRequired()
		}
	}
	
	internal suspend fun checkRequired() {
		val unavailable = hooks
			.filter { it.required }
			.ifEmpty { return }
			.filterNot { it.isHooked }
			.ifEmpty { return }
			.joinToString(prefix = "[", postfix = "]") { it.name }
		val log = "The following dependencies are required by ${plugin.name} but are not available: $unavailable"
		withContext(MCore.serverDispatcher) {
			MCore.instance.componentLogger.error(log)
			plugin.enabled = false
		}
	}
	
	private suspend fun manageHooks() = coroutineScope {
		hooks.forEach { launch { it.init() } }
	}
	
	class Logger internal constructor(var logger: (Component) -> Unit) {
		
		val messages = Messages()
		
		internal fun log(hook: Hook, initial: Boolean) {
			val log = when {
				initial && hook.isHooked -> messages.successful_hook(hook)
				initial -> messages.fail_hook(hook)
				hook.isHooked -> messages.successful_rehook(hook)
				else -> messages.successful_unhook(hook)
			}
			if (log != Component.empty()) logger(log)
		}
		
		@Suppress("PropertyName")
		data class Messages(
			var successful_hook: (Hook) -> Component = { Component.text("Successfully hooked to ${it.name}.") },
			var fail_hook: (Hook) -> Component = { Component.text("Failed to hook to ${it.name}.") },
			var successful_unhook: (Hook) -> Component = { Component.text("Successfully unhooked from ${it.name}.") },
			var successful_rehook: (Hook) -> Component = { Component.text("Successfully rehooked to ${it.name}.") },
		)
	}
	
	companion object {
		
		@JvmStatic
		val Plugin.hooksHandler
			get() = HooksManager.handlers.find { it.plugin == this } ?: HooksHandler(this).also { HooksManager += it }
	}
}