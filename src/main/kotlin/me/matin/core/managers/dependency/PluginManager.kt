package me.matin.core.managers.dependency

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage", "unused")
object PluginManager {

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

    /**
     * @param name Name of the plugin to get
     * @return The [Plugin] with the given name or `null` if not found.
     */
    @JvmStatic
    operator fun get(name: String): Plugin? =
        Bukkit.getPluginManager().getPlugin(name)?.takeIf { it.isEnabled }

    /**
     * Checks the state of the given plugins.
     *
     * @param plugins Plugins to check.
     * @param action Action to run after check. (Gives a set of installed and a
     *    set of missing plugins.)
     */
    @JvmStatic
    fun checkState(plugins: Set<String>, action: (installed: Set<String>, missing: Set<String>) -> Unit) {
        val installed = plugins.filter { get(it) != null }.toSet()
        action(installed, plugins - installed)
    }

    /**
     * Checks the state of the given plugins with version support.
     *
     * @param pluginsWithVersion Plugins to check with their valid versions.
     * @param action Action to run after check. (Gives a set of installed, a
     *    set of missing plugins and a set of plugins with wrong installed
     *    version.)
     */
    @JvmStatic
    fun checkState(
        pluginsWithVersion: Map<String, String>,
        action: (installed: Set<String>, missing: Set<String>, wrongVersion: Set<String>) -> Unit
    ) {
        val installed = mutableSetOf<String>()
        val wrongVersion = mutableSetOf<String>()
        pluginsWithVersion.forEach { (name, versions) ->
            get(name)?.run {
                val isCorrectVersion = versions.takeIf { it.isNotBlank() }?.let { inVersions(it) } != false
                if (isCorrectVersion) installed.add(name) else wrongVersion.add(name)
            }
        }
        action(installed, (pluginsWithVersion.keys - installed) - wrongVersion, wrongVersion)
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