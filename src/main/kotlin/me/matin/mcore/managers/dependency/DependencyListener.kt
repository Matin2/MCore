package me.matin.mcore.managers.dependency

import me.matin.mcore.methods.async
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

internal object DependencyListener: Listener {
	
	val managers: MutableSet<DependencyManager> = mutableSetOf()
	
	@Suppress("UnstableApiUsage")
	fun checkDepend(dependency: Dependency) = dependency.run {
		available = plugin?.run { isEnabled && versionCheck(pluginMeta.version) } == true
	}
	
	fun Plugin.setEnabled(enabled: Boolean) {
		if (isEnabled == enabled) return
		server.pluginManager.also { if (enabled) it.enablePlugin(this) else it.disablePlugin(this) }
	}
	
	private fun check(name: String) = async {
		for (manager in managers) {
			val depend = manager.dependencies.find { it.name == name } ?: continue
			checkDepend(depend)
			if (depend.required) manager.plugin.setEnabled(depend.available)
			break
		}
	}
	
	@EventHandler
	fun onPluginDisable(event: PluginDisableEvent) = check(event.plugin.name)
	
	@EventHandler
	fun onPluginEnable(event: PluginEnableEvent) = check(event.plugin.name)
}