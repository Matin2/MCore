package me.matin.mcore.managers.hook

import kotlinx.coroutines.launch
import me.matin.mcore.MCore.Companion.pluginScope
import me.matin.mcore.managers.hook.HookCheckEvent.CheckState
import me.matin.mcore.managers.hook.HooksListener.setEnabled
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.Plugin

open class HooksManager(internal val plugin: Plugin, logEditor: Logs.() -> Unit = {}) {
	
	val hooks: MutableSet<Hook> = mutableSetOf()
	private val logs = Logs().apply { logEditor() }
	
	fun manage() = pluginScope.launch {
		hooks.forEach { hook ->
			getPluginManager().registerEvents(hook, plugin)
			checkHook(hook, CheckState.INITIAL)
		}
		checkRequired()
		HooksListener.managers.add(this@HooksManager)
	}
	
	internal fun checkHook(hook: Hook, state: CheckState) {
		hook.available = runCatching { hook.plugin }.getOrNull()?.isEnabled == true && hook.checkRequirements()
		plugin.componentLogger.info(logCheckedHook(hook, state == CheckState.INITIAL))
		when (state) {
			CheckState.INITIAL -> HookInitialCheckEvent(hook)
			CheckState.ENABLED -> HookEnableEvent(hook)
			CheckState.DISABLED -> HookDisableEvent(hook)
		}.callEvent()
	}
	
	private fun logCheckedHook(hook: Hook, initial: Boolean) = when {
		initial && hook.available -> logs.successful_hook(hook)
		initial -> logs.fail_hook(hook)
		hook.available -> logs.successful_rehook(hook)
		else -> logs.successful_unhook(hook)
	}
	
	internal fun checkRequired() {
		val unavailable = hooks.filter { it.required }.ifEmpty { return }.filter { !it.available }.map { it.name }
		plugin setEnabled unavailable.isEmpty().also {
			if (it) plugin.componentLogger.info(logs.all_required_available)
		}
	}
	
	@Suppress("PropertyName")
	data class Logs(
		var successful_hook: (Hook) -> Component = { Component.text("Successfully hooked to ${it.name}.") },
		var fail_hook: (Hook) -> Component = { Component.text("Failed to hook to ${it.name}.") },
		var successful_unhook: (Hook) -> Component = { Component.text("Successfully unhooked from ${it.name}.") },
		var successful_rehook: (Hook) -> Component = { Component.text("Successfully rehooked to ${it.name}.") },
		var all_required_available: Component = Component.text("All the required dependencies are installed."),
	) //"${it.joinToString(limit = 3)} are required by ${plugin.name} but are not available."
}