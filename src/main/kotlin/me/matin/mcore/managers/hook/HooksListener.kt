package me.matin.mcore.managers.hook

import kotlinx.coroutines.launch
import me.matin.mcore.MCore.Companion.pluginScope
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEvent
import org.bukkit.plugin.Plugin

internal object HooksListener: Listener {
	
	val managers: MutableSet<HooksManager> = mutableSetOf()
	
	private fun check(plugin: Plugin) = pluginScope.launch {
		for (manager in managers) {
			val hook = manager.hooks.find { it.plugin == plugin } ?: continue
			launch {
				hook.check()
				manager.logState(hook, false)
				if (hook.required) manager.checkRequired()
			}
		}
	}
	
	@EventHandler
	fun PluginEvent.onPluginEnableDisable() {
		check(plugin)
	}
}