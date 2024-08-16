package me.matin.core.managers.dependency

import me.matin.core.Core
import me.matin.core.managers.TaskManager.schedule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

@Suppress("unused")
object DependencyListener: Listener {

    val monitoredPlugins: MutableMap<Map<String, String>, Plugin.(Set<String>, Set<String>, Set<String>) -> Unit> =
        mutableMapOf()

    private fun monitor() {
        schedule(true) {
            monitoredPlugins.forEach { (dependencies, action) ->
                PluginManager.checkState(dependencies) { installed, missing, wrongVersion ->
                    schedule {
                        Core.instance.action(installed, missing, wrongVersion)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) = monitor()

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) = monitor()
}