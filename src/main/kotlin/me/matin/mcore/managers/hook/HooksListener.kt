package me.matin.mcore.managers.hook

import me.matin.mcore.MCore
import me.matin.mcore.methods.async
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

internal object HooksListener: Listener {
	
	val managers: MutableSet<HooksManager> = mutableSetOf()
	
	@Suppress("UnstableApiUsage")
	fun checkHook(hook: Hook) = hook.run {
		available = plugin?.run { isEnabled && versionCheck(pluginMeta.version) } == true
	}
	
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
	
	private fun check(name: String) = async {
		for (manager in managers) {
			val depend = manager.hooks.find { it.name == name } ?: continue
			checkHook(depend)
			if (!depend.required) continue
			manager.checkRequired {
				someUnavailable =
					"${unavailable.joinToString(limit = 3)} are required by ${manager.plugin.name} but are not available."
			}
		}
	}
	
	@EventHandler
	fun onPluginDisable(event: PluginDisableEvent) = check(event.plugin.name)
	
	@EventHandler
	fun onPluginEnable(event: PluginEnableEvent) = check(event.plugin.name)
}