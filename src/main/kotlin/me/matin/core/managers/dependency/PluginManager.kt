package me.matin.core.managers.dependency

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage", "unused")
object PluginManager {

    @JvmStatic
    operator fun get(pluginName: String): Plugin? = Bukkit.getPluginManager().getPlugin(pluginName)?.takeIf { it.isEnabled }

    var Plugin.enabled: Boolean get() { return this.isEnabled } set(enable) {
        when {
            enable && !this.isEnabled -> Bukkit.getPluginManager().enablePlugin(this)
            !enable && this.isEnabled -> Bukkit.getPluginManager().disablePlugin(this)
        }
    }

    @JvmStatic
    fun isInstalled(pluginName: String): Boolean = get(pluginName) != null

    @JvmStatic
    fun areInstalled(pluginNames: Array<String>): Boolean = pluginNames.map { get(it) != null }.all { it }

    @JvmStatic
    fun checkState(dependencies: Set<String>, monitor: Boolean = false, action: (name: String, installed: Boolean) -> Unit) {
        dependencies.forEach {
            action(it, isInstalled(it))
        }
        if (monitor) monitorState(dependencies) { name, installed -> action(name, installed) }
    }

    @JvmStatic
    fun checkState(dependenciesWithVersion: Map<String, String>, monitor: Boolean = false, action: (name: String, state: DependencyState) -> Unit) {
        dependenciesWithVersion.forEach { (name, versions) ->
            val plugin = get(name)
            plugin ?: run {
                action(name, DependencyState.NOT_INSTALLED)
                return@forEach
            }
            action(name, versions.takeIf { it.isNotBlank() }?.let { plugin.checkVersions(it) } ?: DependencyState.INSTALLED)
        }
        if (monitor) monitorState(dependenciesWithVersion) { name, state -> action(name, state) }
    }

    @JvmStatic
    fun monitorState(dependencies: Set<String>, action: Plugin.(name: String, installed: Boolean) -> Unit) = DependencyListener.monitoredPlugins.add(MonitoredPlugin(dependencies, action))

    @JvmStatic
    fun monitorState(dependenciesWithVersion: Map<String, String>, action: Plugin.(name: String, state: DependencyState) -> Unit) = DependencyListener.monitoredPlugins.add(MonitoredPlugin(dependenciesWithVersion, action))

    internal fun Plugin.checkVersions(versions: String): DependencyState {
        val version = this.pluginMeta.version.uppercase()
        val state = versions.split('\\').map {
            it.uppercase().run {
                when {
                    startsWith('*') -> version.contains(removePrefix("*"))
                    startsWith('>') -> version.startsWith(removePrefix(">"))
                    startsWith('<') -> version.endsWith(removePrefix("<"))
                    startsWith("!*") -> !version.contains(removePrefix("!*"))
                    startsWith("!>") -> version.startsWith(removePrefix("!>"))
                    startsWith("!<") -> version.endsWith(removePrefix("!<"))
                    startsWith('!') -> version != removePrefix("!")
                    else -> version == this
                }
            }
        }.any { it }
        return if (state) DependencyState.INSTALLED else DependencyState.WRONG_VERSION
    }
}