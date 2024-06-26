package me.matin.core.managers.dependency

import me.matin.core.Core
import me.matin.core.managers.dependency.PluginManager.checkVersions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.event.server.PluginEvent

@Suppress("unused")
object DependencyListener: Listener {

    val monitoredPlugins = emptySet<MonitoredPlugin>() as MutableSet

    private fun monitor(event: PluginEvent) {
        monitoredPlugins.forEach { (dependencies, action) ->
            val dependency = event.plugin.takeIf { it.name in dependencies.keys } ?: return
            when (event) {
                is PluginDisableEvent -> Core.plugin.action(dependency.name, DependencyState.NOT_INSTALLED)
                is PluginEnableEvent -> dependencies.also {
                    Core.plugin.action(dependency.name, dependency.checkVersions(dependencies[dependency.name]!!))
                }
            }

        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) = monitor(event)

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) = monitor(event)
}