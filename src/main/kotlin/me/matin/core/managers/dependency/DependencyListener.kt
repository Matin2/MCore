package me.matin.core.managers.dependency

import me.matin.core.managers.dependency.DependencyManager.checkVersions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

@Suppress("LocalVariableName", "unused")
class DependencyListener(private val plugins: Set<String>, private val action: (String, CheckedDepend) -> Unit): Listener {

    private var pluginVersions: Map<String, String>? = null

    constructor(plugin_versions: Map<String, String>, action: (String, CheckedDepend) -> Unit): this(plugin_versions.keys, action) {
        this.pluginVersions = plugin_versions
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        if (event.plugin.name !in plugins) return
        action(event.plugin.name, CheckedDepend.NOT_INSTALLED)
    }

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        if (event.plugin.name !in plugins) return
        val name = event.plugin.name
        pluginVersions?.let {
            action(name, if (event.plugin.checkVersions(pluginVersions!![name]!!))
                CheckedDepend.INSTALLED else CheckedDepend.WRONG_VERSION)
        } ?: run { action(name, CheckedDepend.INSTALLED) }
    }
}