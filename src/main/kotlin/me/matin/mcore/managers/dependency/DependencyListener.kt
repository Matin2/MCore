package me.matin.mcore.managers.dependency

import me.matin.mcore.MCore
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
	
	infix fun Plugin.setEnabled(enabled: Boolean) {
		if (isEnabled == enabled) return
		if (enabled) {
			server.pluginManager.enablePlugin(this)
			MCore.instance.logger.info("${this.name} got enabled.")
			return
		}
		server.pluginManager.disablePlugin(this)
		MCore.instance.logger.info("${this.name} got disabled.")
	}
	
	private fun check(name: String) = async {
		for (manager in managers) {
			val depend = manager.dependencies.find { it.name == name } ?: continue
			checkDepend(depend)
			if (!depend.required) continue
			manager.plugin.setEnabled(depend.available)
		}
	}
	
	@EventHandler
	fun onPluginDisable(event: PluginDisableEvent) = check(event.plugin.name)
	
	@EventHandler
	fun onPluginEnable(event: PluginEnableEvent) = check(event.plugin.name)
}