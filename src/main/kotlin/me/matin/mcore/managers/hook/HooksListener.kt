package me.matin.mcore.managers.hook

import kotlinx.coroutines.launch
import me.matin.mcore.MCore
import me.matin.mcore.MCore.Companion.pluginScope
import me.matin.mcore.managers.hook.HookCheckEvent.CheckState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

internal object HooksListener: Listener {
	
	val managers: MutableSet<HooksManager> = mutableSetOf()
	
	infix fun Plugin.setEnabled(enabled: Boolean) {
		if (isEnabled == enabled) return
		if (enabled) {
			server.pluginManager.enablePlugin(this)
			MCore.instance.logger.info("$name got enabled.")
			return
		}
		server.pluginManager.disablePlugin(this)
		MCore.instance.logger.info("$name got disabled.")
	}
	
	private fun check(name: String, onEnable: Boolean) {
		pluginScope.launch {
			for (manager in managers) {
				val hook = manager.hooks.find { it.name == name } ?: continue
				manager.checkHook(hook, if (onEnable) CheckState.ENABLED else CheckState.DISABLED)
				if (hook.required) manager.checkRequired()
			}
		}
	}
	
	@EventHandler
	fun PluginEnableEvent.onPluginEnable() = check(plugin.name, true)
	
	@EventHandler
	fun PluginDisableEvent.onPluginDisable() = check(plugin.name, false)
}