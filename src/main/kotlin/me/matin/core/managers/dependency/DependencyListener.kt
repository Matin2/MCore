package me.matin.core.managers.dependency

import me.matin.core.managers.dependency.DependencyManager.checkVersions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

@Suppress("unused")
class DependencyListener: Listener {

    private var pluginToVersions: Map<String, String>? = null
    private var plugins: Set<String>? = null

    private var versionAction: ((String, DependencyState) -> Unit)? = null
    private var noVersionAction: ((String, Boolean) -> Unit)? = null
    private val action: (String, DependencyState) -> Unit = { name, state ->
        versionAction?.let { it(name, state) } ?: noVersionAction?.let { it(name, state.value) }
    }

    constructor(pluginToVersions: Map<String, String>, action: (name: String, state: DependencyState) -> Unit) {
        this.pluginToVersions = pluginToVersions
        this.versionAction = action
    }
    constructor(plugins: Set<String>, action: (name: String, enabled: Boolean) -> Unit) {
        this.plugins = plugins
        this.noVersionAction = action
    }

    private fun isDepend(name: String): Boolean {
        plugins?.let {
            return it.contains(name)
        } ?: pluginToVersions?.keys?.let {
            return it.contains(name)
        } ?: return false
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        if (!isDepend(event.plugin.name)) return
        action(event.plugin.name, DependencyState.NOT_INSTALLED)
    }

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        if (!isDepend(event.plugin.name)) return
        val name = event.plugin.name
        pluginToVersions?.let {
            action(name, event.plugin.checkVersions(pluginToVersions!![name]!!))
        } ?: action(name, DependencyState.INSTALLED)
    }
}