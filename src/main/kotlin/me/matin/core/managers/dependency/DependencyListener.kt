package me.matin.core.managers.dependency

import me.matin.core.Core
import me.matin.core.methods.schedule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

private typealias MonitoredDepends = MutableMap<Dependency, StateChangeAction>

@Suppress("unused")
object DependencyListener: Listener {

    val monitoredPlugins: MonitoredDepends = mutableMapOf()

    private fun check(name: String) = schedule(true) {
        for ((dependency, action) in monitoredPlugins) {
            if (dependency.name != name) continue
            val newState = dependency.state
            schedule { action(Core.instance, newState) }
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) = check(event.plugin.name)

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) = check(event.plugin.name)
}