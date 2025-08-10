package me.matin.mcore.managers.hook

import kotlinx.coroutines.launch
import me.matin.mcore.MCore.Companion.pluginScope
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

internal object HooksListener: Listener {
	
	@JvmStatic
	val managers: MutableSet<HooksManager> = mutableSetOf()
	
	@JvmStatic
	private fun check(plugin: Plugin) = pluginScope.launch {
		for (manager in managers) {
			val hook = manager.hooks.find { it.plugin == plugin } ?: continue
			launch {
				hook.check(false)
				manager.logState(hook, false)
				if (hook.required) manager.checkRequired()
			}
		}
	}
	
	@EventHandler
	fun PluginEnableEvent.onPluginEnable() = check(plugin).let {}
	
	@EventHandler
	fun PluginDisableEvent.onPluginDisable() = check(plugin).let {}
}