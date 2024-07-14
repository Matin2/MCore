package me.matin.core.managers.dependency

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage", "unused")
object PluginManager {

    @JvmStatic
    operator fun get(pluginName: String): Plugin? =
        Bukkit.getPluginManager().getPlugin(pluginName)?.takeIf { it.isEnabled }

    var Plugin.enabled: Boolean
        get() {
            return this.isEnabled
        }
        set(enable) {
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
    fun checkState(dependencies: Set<String>, action: (installed: Set<String>, missing: Set<String>) -> Unit) {
        val installed = dependencies.filter { isInstalled(it) }.toSet()
        action(installed, dependencies - installed)
    }

    @JvmStatic
    fun checkState(
        dependenciesWithVersion: Map<String, String>,
        action: (installed: Set<String>, missing: Set<String>, wrongVersion: Set<String>) -> Unit
    ) {
        val installed = mutableSetOf<String>()
        val wrongVersion = mutableSetOf<String>()
        dependenciesWithVersion.forEach { (name, versions) ->
            get(name)?.run {
                val isCorrectVersion = versions.takeIf { it.isNotBlank() }?.let { inVersions(it) } ?: true
                if (isCorrectVersion) installed.add(name) else wrongVersion.add(name)
            }
        }
        action(installed, (dependenciesWithVersion.keys - installed) - wrongVersion, wrongVersion)
    }

    @JvmStatic
    fun monitorState(dependencies: Set<String>, action: Plugin.(installed: Set<String>, missing: Set<String>) -> Unit) {
        val dependenciesWithVersion = dependencies.zip(Array(dependencies.size) { "" }).toMap()
        DependencyListener.monitoredPlugins[dependenciesWithVersion] = { installed, missing, _ ->
            this.action(installed, missing)
        }
    }

    @JvmStatic
    fun monitorState(
        dependenciesWithVersion: Map<String, String>,
        action: Plugin.(installed: Set<String>, missing: Set<String>, wrongVersion: Set<String>) -> Unit
    ) {
        DependencyListener.monitoredPlugins[dependenciesWithVersion] = action
    }

    private fun Plugin.inVersions(versions: String): Boolean {
        val version = this.pluginMeta.version.uppercase()
        return versions.split('\\').map {
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
    }
}