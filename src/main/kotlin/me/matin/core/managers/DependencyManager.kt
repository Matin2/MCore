package me.matin.core.managers

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage", "unused")
object DependencyManager {

    @JvmStatic
    fun isPluginInstalled(pluginName: String): Boolean {
        val plugin = Bukkit.getPluginManager().getPlugin(pluginName)
        return plugin != null && plugin.isEnabled
    }

    @JvmStatic
    fun arePluginsInstalled(pluginNames: Array<String>): Boolean {
        val statues: MutableList<Boolean> = ArrayList()
        for (pluginName in pluginNames) {
            val plugin = Bukkit.getPluginManager().getPlugin(pluginName.trim())
            if (plugin != null && plugin.isEnabled) {
                statues.add(true)
            } else {
                statues.add(false)
            }
        }
        for (b in statues) if (!b) return false
        return true
    }

    @JvmStatic
    fun checkDepends(plugin: Plugin, depends: Array<String>, warning: String = "{plugin} is required but not installed!") {
        for (pluginName in depends) {
            if (isPluginInstalled(pluginName)) continue
            plugin.logger.warning(warning.replace("{plugin}", pluginName))
            Bukkit.getPluginManager().disablePlugin(plugin)
        }
    }

    @JvmStatic
    fun checkDepends(plugin: Plugin, warning: String = "{plugin} is required but not installed!") {
        for (pluginName in plugin.pluginMeta.pluginDependencies) {
            if (pluginName == null) continue
            if (isPluginInstalled(pluginName)) continue
            plugin.logger.warning(warning.replace("{plugin}", pluginName))
            Bukkit.getPluginManager().disablePlugin(plugin)
        }
    }
}