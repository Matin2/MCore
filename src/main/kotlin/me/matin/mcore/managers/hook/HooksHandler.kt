package me.matin.mcore.managers.hook

import kotlinx.coroutines.*
import me.matin.mcore.dispatchers
import me.matin.mcore.mcore
import me.matin.mcore.methods.enabled
import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin

@Suppress("unused", "NOTHING_TO_INLINE")
class HooksHandler internal constructor(internal val plugin: Plugin) {
	
	val hooks: Set<Hook>
		field: MutableSet<Hook> = mutableSetOf()
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
	
	fun registerAll(hooks: Iterable<Hook>): Boolean {
		val hookNames = hooks.map { it.name }
		return hooks.filter { it.name in hookNames }.ifEmpty { return false }.let { this.hooks.addAll(it) }
	}
	
	inline fun registerAll(vararg hooks: Hook): Boolean = registerAll(hooks.toList())
	
	inline operator fun plusAssign(hook: Hook) {
		register(hook)
	}
	
	inline operator fun plusAssign(hooks: Iterable<Hook>) {
		registerAll(hooks)
	}
	
	internal fun init() {
		HooksManager += this
		scope = CoroutineScope(HooksManager.coroutineContext + Job(HooksManager.coroutineContext.job))
		scope.launch {
			manageHooks()
			checkRequired()
		}
	}
	
	internal fun disable() {
		HooksManager -= this
		scope.cancel(CancellationException("Plugin ${plugin.name} has been disabled."))
	}
	
	internal suspend fun checkRequired() {
		val unavailable = hooks
			.filter { it.required }
			.ifEmpty { return }
			.filterNot { it.isHooked }
			.ifEmpty { return }
			.joinToString(prefix = "[", postfix = "]") { it.name }
			.let { "The following dependencies are required by ${plugin.name} but are not available: $it" }
		withContext(dispatchers.main) {
			mcore.componentLogger.error(unavailable)
			plugin.enabled = false
		}
	}
	
	private suspend fun manageHooks() = coroutineScope {
		hooks.forEach { launch { it.init() } }
	}
	
	class Logger internal constructor(var enabled: Boolean = true, var logger: (Component) -> Unit) {
		
		val messages = Messages()
		
		internal fun log(hook: Hook, initial: Boolean) = when {
			initial && hook.isHooked -> messages.hooked(hook)
			initial -> messages.hookFailed(hook)
			hook.isHooked -> messages.rehooked(hook)
			else -> messages.unhooked(hook)
		}.takeUnless { it == Component.empty() }?.let { logger(it) } ?: Unit
		
		data class Messages(
			var hooked: (Hook) -> Component = { Component.text("Successfully hooked to ${it.name}.") },
			var hookFailed: (Hook) -> Component = { Component.text("Failed to hook to ${it.name}.") },
			var unhooked: (Hook) -> Component = { Component.text("Unhooked from ${it.name}.") },
			var rehooked: (Hook) -> Component = { Component.text("Rehooked to ${it.name}.") },
		)
	}
}