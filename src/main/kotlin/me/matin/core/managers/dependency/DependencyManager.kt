package me.matin.core.managers.dependency

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage", "unused")
object DependencyManager {

    @JvmStatic
    operator fun get(pluginName: String): Plugin? = Bukkit.getPluginManager().getPlugin(pluginName)?.takeIf { it.isEnabled }

    @JvmStatic
    operator fun set(plugin: Plugin, enable: Boolean) {
        when {
            enable && !plugin.isEnabled -> Bukkit.getPluginManager().enablePlugin(plugin)
            !enable && plugin.isEnabled -> Bukkit.getPluginManager().disablePlugin(plugin)
        }
    }

    @JvmStatic
    operator fun set(pluginName: String, enable: Boolean) {
        return set(get(pluginName) ?: return, enable)
    }

    @JvmStatic
    fun isPluginInstalled(pluginName: String): Boolean = get(pluginName) != null

    @JvmStatic
    fun arePluginsInstalled(pluginNames: Array<String>): Boolean = pluginNames.map { get(it) != null }.all { it }

    @JvmStatic
    fun checkDepends(plugins: Set<String>, registerListener: Plugin? = null, action: (name: String, installed: Boolean) -> Unit) {
        plugins.forEach {
            action(it, isPluginInstalled(it))
        }
        Bukkit.getPluginManager().registerEvents(DependencyListener(plugins, action), registerListener ?: return)
    }

    @JvmStatic
    fun checkDepends(pluginsVersions: Map<String, String>, registerListener: Plugin? = null, action: (name: String, state: DependencyState) -> Unit) {
        pluginsVersions.forEach { (name, versions) ->
            val plugin = get(name)
            plugin ?: run {
                action(name, DependencyState.NOT_INSTALLED)
                return@forEach
            }
            action(name, versions.takeIf { it.isNotBlank() }?.let { plugin.checkVersions(it) } ?: DependencyState.INSTALLED)
        }
        Bukkit.getPluginManager().registerEvents(DependencyListener(pluginsVersions, action), registerListener ?: return)
    }

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