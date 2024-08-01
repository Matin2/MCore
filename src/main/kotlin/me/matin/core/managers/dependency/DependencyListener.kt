package me.matin.core.managers.dependency

import me.matin.core.Core
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

@Suppress("unused")
object DependencyListener: Listener {

    val monitoredPlugins: MutableMap<Map<String, String>, Plugin.(Set<String>, Set<String>, Set<String>) -> Unit> =
        mutableMapOf()

    private fun monitor() = Core.scheduleTask(true) {
        monitoredPlugins.forEach { (dependencies, action) ->
            PluginManager.checkState(dependencies) { installed, missing, wrongVersion ->
                Core.scheduleTask {
                    Core.instance.action(installed, missing, wrongVersion)
                }
            }
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        monitor()
    }

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        monitor()
    }
}