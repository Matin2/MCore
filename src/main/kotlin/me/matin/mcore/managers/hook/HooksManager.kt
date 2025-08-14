package me.matin.mcore.managers.hook

import kotlinx.coroutines.*
import me.matin.mcore.MCore
import me.matin.mcore.methods.enabled
import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin
import kotlin.properties.Delegates

open class HooksManager(internal val plugin: Plugin, vararg hooks: Hook, config: Config.() -> Unit = {}) {
	
	val hooks: MutableSet<Hook> = hooks.toMutableSet()
	lateinit var scope: CoroutineScope
	private val config = Config { plugin.componentLogger.info(it) }.apply(config)
	private var pluginEnabled by Delegates.vetoable(true) { _, old, new ->
		when (new) {
			old -> false
			true if this.config.enable_plugin_on_all_required_rehooked -> true
			false if this.config.disable_plugin_on_some_required_not_hooked -> true
			else -> false
		}.also { if (it) plugin.enabled = new }
	}
	
	fun manageEnable() {
		scope = CoroutineScope(HooksHandler.scope.coroutineContext + Job(HooksHandler.scope.coroutineContext.job))
		scope.launch {
			launch { manageHooks() }.join()
			onCheck(true)
		}
	}
	
	fun manageDisable() {
		if (config.enable_plugin_on_all_required_rehooked) scope.launch {
			HooksHandler.removeManager(this@HooksManager)
		}.invokeOnCompletion {
			scope.cancel(CancellationException("Plugin ${plugin.name} has been disabled."))
		}
	}
	
	private suspend fun manageHooks() = hooks.forEach { hook ->
		val instance = HooksHandler.hooks.keys.find { (name, requirements) ->
			name == hook.name && requirements == hook.requirements
		}?.also { HooksHandler.addManagerToInstance(this, it) } ?: HookInstance(hook).also {
			HooksHandler.addInstance(it, this)
		}
		hook.instance = instance
		hook.init(plugin)
	}
	
	internal suspend fun onCheck(initial: Boolean): Unit = coroutineScope {
		launch { checkRequired() }
		hooks.forEach {
			launch {
				if (initial) it.initialCheck.join()
				it.logState(initial)
			}
		}
	}
	
	internal fun checkRequired() {
		val unavailable = hooks.filter { it.required }.ifEmpty { return }.filterNot { it.isHooked }
		if (unavailable.isEmpty()) {
			pluginEnabled = true
			return
		}
		if (!pluginEnabled) return
		val unavailableNames = unavailable.joinToString(prefix = "[", postfix = "]") { it.name }
		val log = "The following dependencies are required by ${plugin.name} but are not available: $unavailableNames"
		MCore.instance.componentLogger.error(log)
		pluginEnabled = false
	}
	
	private fun Hook.logState(initial: Boolean) {
		val log = when {
			initial && isHooked -> config.logs.successful_hook(this)
			initial -> config.logs.fail_hook(this)
			isHooked -> config.logs.successful_rehook(this)
			else -> config.logs.successful_unhook(this)
		}
		if (log != Component.empty()) config.infoLogger(log)
	}
	
	@Suppress("PropertyName")
	class Config internal constructor(
		var enable_plugin_on_all_required_rehooked: Boolean = false,
		var disable_plugin_on_some_required_not_hooked: Boolean = true,
		var infoLogger: (Component) -> Unit,
	) {
		
		val logs = Logs()
		
		data class Logs(
			var successful_hook: (Hook) -> Component = { Component.text("Successfully hooked to ${it.name}.") },
			var fail_hook: (Hook) -> Component = { Component.text("Failed to hook to ${it.name}.") },
			var successful_unhook: (Hook) -> Component = { Component.text("Successfully unhooked from ${it.name}.") },
			var successful_rehook: (Hook) -> Component = { Component.text("Successfully rehooked to ${it.name}.") },
		)
	}
}