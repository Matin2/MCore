package me.matin.mcore.managers.hook

import kotlinx.coroutines.launch
import me.matin.mcore.MCore
import me.matin.mcore.MCore.Companion.pluginScope
import me.matin.mcore.methods.enabled
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.Plugin

open class HooksManager(internal val plugin: Plugin, vararg hooks: Hook, logEditor: Logs.() -> Unit = {}) {
	
	val hooks: MutableSet<Hook> = hooks.toMutableSet()
	private val logs = Logs(plugin.componentLogger).apply { logEditor() }
	
	fun manage() = pluginScope.launch {
		hooks.forEach { hook ->
			hook.initialize()
			logState(hook, true)
			getPluginManager().registerEvents(hook, plugin)
		}
		checkRequired()
		HooksListener.managers.add(this@HooksManager)
	}
	
	internal fun checkRequired() {
		val unavailable =
			hooks.filter { it.required }.ifEmpty { return }.filterNot { it.isAvailable }
		val available = unavailable.isEmpty()
		if (available) {
			MCore.instance.componentLogger.error(
				"""The following dependencies are required by ${plugin.name} but are not available: ${
					unavailable.joinToString(prefix = "[", postfix = "]") { it.name }
				}""")
		} else {
			logs.logger.info(logs.all_required_available)
		}
		plugin.enabled = available
	}
	
	internal fun logState(hook: Hook, initial: Boolean) = when {
		initial && hook.isAvailable -> logs.successful_hook(hook)
		initial -> logs.fail_hook(hook)
		hook.isAvailable -> logs.successful_rehook(hook)
		else -> logs.successful_unhook(hook)
	}.takeUnless { it == Component.empty() }?.let { logs.logger.info(it) } ?: Unit
	
	@Suppress("PropertyName")
	data class Logs(
		var logger: ComponentLogger,
		var successful_hook: (Hook) -> Component = { Component.text("Successfully hooked to ${it.name}.") },
		var fail_hook: (Hook) -> Component = { Component.text("Failed to hook to ${it.name}.") },
		var successful_unhook: (Hook) -> Component = { Component.text("Successfully unhooked from ${it.name}.") },
		var successful_rehook: (Hook) -> Component = { Component.text("Successfully rehooked to ${it.name}.") },
		var all_required_available: Component = Component.text("All the required dependencies are installed."),
	)
}