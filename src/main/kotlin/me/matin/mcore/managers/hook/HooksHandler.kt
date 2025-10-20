package me.matin.mcore.managers.hook

import kotlinx.coroutines.*
import me.matin.mcore.MCore
import me.matin.mcore.methods.enabled
import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin

@Suppress("unused")
class HooksHandler private constructor(internal val plugin: Plugin) {
	
	val hooks: MutableSet<Hook> = mutableSetOf()
	lateinit var scope: CoroutineScope private set
	private var logger = Logger { plugin.componentLogger.info(it) }
	
	fun configureLogger(config: Logger.() -> Unit) {
		logger = logger.apply(config)
	}
	
	internal suspend fun onCheck(initial: Boolean): Unit = coroutineScope {
		launch { checkRequired() }
		hooks.forEach { launch { logger.log(it, initial) } }
	}
	
	internal fun onPluginStateChange(onDisable: Boolean) {
		if (onDisable) {
			HooksManager -= this
			scope.cancel(CancellationException("Plugin ${plugin.name} has been disabled."))
			return
		}
		scope = CoroutineScope(HooksManager.scope.coroutineContext + Job(HooksManager.scope.coroutineContext.job))
		scope.launch {
			launch { manageHooks() }.join()
			onCheck(true)
		}
	}
	
	private suspend fun manageHooks() = hooks.forEach { hook ->
		hook.instance = HooksManager.hookInstances.find { (name, requirements) ->
			name == hook.name && requirements == hook.requirements
		} ?: HookInstance(hook).also { HooksManager += it }
		hook.init(plugin)
	}
	
	private fun checkRequired() {
		val unavailable = hooks.filter { it.required }.ifEmpty { return }.filterNot { it.isHooked }.ifEmpty { return }
		val unavailableNames = unavailable.joinToString(prefix = "[", postfix = "]") { it.name }
		val log = "The following dependencies are required by ${plugin.name} but are not available: $unavailableNames"
		MCore.instance.componentLogger.error(log)
		plugin.enabled = false
	}
	
	class Logger internal constructor(var logger: (Component) -> Unit) {
		
		val messages = Messages()
		
		internal suspend fun log(hook: Hook, initial: Boolean) {
			val log = when {
				initial && hook.isHooked -> messages.successful_hook(hook)
				initial -> messages.fail_hook(hook)
				hook.isHooked -> messages.successful_rehook(hook)
				else -> messages.successful_unhook(hook)
			}
			if (initial) hook.initialCheck.join()
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