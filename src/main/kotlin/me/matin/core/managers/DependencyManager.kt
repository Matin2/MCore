package me.matin.core.managers

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class DependencyManager {

    companion object {

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
        fun getPlugin(pluginName: String): Plugin? {
            return Bukkit.getPluginManager().getPlugin(pluginName)
        }

        @JvmStatic
        fun checkDepends(plugin: Plugin, depends: Array<String>) {
            for (pluginName in depends) {
                if (!isPluginInstalled(pluginName)) {
                    plugin.logger.warning("$pluginName is required but not installed!")
                    Bukkit.getPluginManager().disablePlugin(plugin)
                }
            }
        }

        @JvmStatic
        fun checkDepends(plugin: Plugin) {
            for (pluginName in plugin.pluginMeta.pluginDependencies) {
                if (pluginName == null) continue
                if (isPluginInstalled(pluginName)) continue
                plugin.logger.warning("$pluginName is required but not installed!")
                Bukkit.getPluginManager().disablePlugin(plugin)
            }
        }
    }
}