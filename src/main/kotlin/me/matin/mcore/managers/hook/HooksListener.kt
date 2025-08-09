package me.matin.mcore.managers.hook

import kotlinx.coroutines.launch
import me.matin.mcore.MCore.Companion.pluginScope
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

internal object HooksListener: Listener {
	
	val managers: MutableSet<HooksManager> = mutableSetOf()
	
	infix fun Plugin.setEnabled(enabled: Boolean) = when (enabled) {
		isEnabled -> Unit
		true -> server.pluginManager.enablePlugin(this)
		false -> server.pluginManager.disablePlugin(this)
	}
	
	private fun check(plugin: Plugin, onEnable: Boolean) {
		pluginScope.launch {
			for (manager in managers) {
				val hook = manager.hooks.find { it.plugin == plugin } ?: continue
				hook.updateState()
				val event = if (onEnable) HookEnableEvent(hook) else HookDisableEvent(hook)
				event.callEvent()
				manager.logState(hook, false)
				if (hook.required) manager.checkRequired()
			}
		}
	}
	
	@EventHandler
	fun PluginEnableEvent.onPluginEnable() = check(plugin, true)
	
	@EventHandler
	fun PluginDisableEvent.onPluginDisable() = check(plugin, false)
}