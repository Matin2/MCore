package me.matin.mcore.managers.hook

import kotlinx.coroutines.*
import me.matin.mcore.MCore
import me.matin.mcore.methods.enabled
import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin
import kotlin.properties.Delegates

@Suppress("unused")
class HooksHandler private constructor(internal val plugin: Plugin) {
	
	val hooks: MutableSet<Hook> = mutableSetOf()
	lateinit var scope: CoroutineScope private set
	private var config = Config { plugin.componentLogger.info(it) }
	private var pluginEnabled by Delegates.vetoable(true) { _, old, new ->
		when (new) {
			old -> false
			true if config.enable_plugin_on_all_required_rehooked -> true
			false if config.disable_plugin_on_some_required_not_hooked -> true
			else -> false
		}.also { if (it) plugin.enabled = new }
	}
	
	fun configure(configuration: Config.() -> Unit) {
		config = config.apply(configuration)
	}
	
	internal fun checkPluginState(enabled: Boolean) {
		if (pluginEnabled == enabled) return
		if (enabled) onPluginEnable() else onPluginDisable()
	}
	
	internal suspend fun onCheck(initial: Boolean): Unit = coroutineScope {
		launch { checkRequired() }
		hooks.forEach { launch { it.logState(initial) } }
	}
	
	private fun onPluginEnable() {
		scope = CoroutineScope(HooksManager.scope.coroutineContext + Job(HooksManager.scope.coroutineContext.job))
		scope.launch {
			launch { manageHooks() }.join()
			onCheck(true)
		}
	}
	
	private fun onPluginDisable() {
		HooksManager -= this
		scope.cancel(CancellationException("Plugin ${plugin.name} has been disabled."))
	}
	
	private suspend fun manageHooks() = hooks.forEach { hook ->
		hook.instance = HooksManager.hookInstances.find { (name, requirements) ->
			name == hook.name && requirements == hook.requirements
		} ?: HookInstance(hook).also { HooksManager += it }
		hook.init(plugin)
	}
	
	private fun checkRequired() {
		val unavailable = hooks.filter { it.required }.ifEmpty { return }.filterNot { it.isHooked }
		unavailable.ifEmpty { pluginEnabled = true; return }
		if (!pluginEnabled) return
		val unavailableNames = unavailable.joinToString(prefix = "[", postfix = "]") { it.name }
		val log = "The following dependencies are required by ${plugin.name} but are not available: $unavailableNames"
		MCore.instance.componentLogger.error(log)
		pluginEnabled = false
	}
	
	private suspend fun Hook.logState(initial: Boolean) {
		val log = when {
			initial && isHooked -> config.logs.successful_hook(this)
			initial -> config.logs.fail_hook(this)
			isHooked -> config.logs.successful_rehook(this)
			else -> config.logs.successful_unhook(this)
		}
		if (initial) initialCheck.join()
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
	
	companion object {
		
		@JvmStatic
		val Plugin.hooksHandler
			get() = HooksManager.handlers.find { it.plugin == this } ?: HooksHandler(this).also { HooksManager += it }
	}
}