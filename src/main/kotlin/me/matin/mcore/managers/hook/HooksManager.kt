package me.matin.mcore.managers.hook

import kotlinx.coroutines.launch
import me.matin.mcore.MCore.Companion.pluginScope
import me.matin.mcore.managers.hook.HooksListener.setEnabled
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.Plugin

open class HooksManager(internal val plugin: Plugin, logEditor: Logs.() -> Unit = {}) {
	
	val hooks: MutableSet<Hook> = mutableSetOf()
	private val logs = Logs().apply { logEditor() }
	
	fun manage() = pluginScope.launch {
		hooks.forEach { hook ->
			hook.init()
			logState(hook, true)
			getPluginManager().registerEvents(hook, plugin)
		}
		checkRequired()
		HooksListener.managers.add(this@HooksManager)
	}
	
	internal fun checkRequired() {
		val unavailable =
			hooks.filter { it.required }.ifEmpty { return }.filterNot { it.available }
		val available = unavailable.isEmpty()
		if (available) {
			plugin.componentLogger.error(
				"""The following dependencies are required by ${plugin.name} but are not available: ${
					unavailable.joinToString(prefix = "[", postfix = "]") { it.name }
				}""")
		} else {
			plugin.componentLogger.info(logs.all_required_available)
		}
		plugin setEnabled available
	}
	
	internal fun logState(hook: Hook, initial: Boolean) = plugin.componentLogger.info(
		when {
			initial && hook.available -> logs.successful_hook(hook)
			initial -> logs.fail_hook(hook)
			hook.available -> logs.successful_rehook(hook)
			else -> logs.successful_unhook(hook)
		}
	)
	
	@Suppress("PropertyName")
	data class Logs(
		var successful_hook: (Hook) -> Component = { Component.text("Successfully hooked to ${it.name}.") },
		var fail_hook: (Hook) -> Component = { Component.text("Failed to hook to ${it.name}.") },
		var successful_unhook: (Hook) -> Component = { Component.text("Successfully unhooked from ${it.name}.") },
		var successful_rehook: (Hook) -> Component = { Component.text("Successfully rehooked to ${it.name}.") },
		var all_required_available: Component = Component.text("All the required dependencies are installed."),
	)
}