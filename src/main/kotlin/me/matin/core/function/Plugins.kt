package me.matin.core.function

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class Plugins {

    companion object {

        @JvmStatic
        fun hasPlugin(pluginName: String): Boolean {
            val plugin = Bukkit.getPluginManager().getPlugin(pluginName)
            return plugin != null && plugin.isEnabled
        }

        @JvmStatic
        fun hasPlugins(pluginNames: String): Boolean {
            val pluginNames1: Array<String> = pluginNames.split(',').dropLastWhile { it.isEmpty() }.toTypedArray()
            return hasPlugins(pluginNames1)
        }

        @JvmStatic
        fun hasPlugins(pluginNames: Array<String>): Boolean {
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
                if (!hasPlugin(pluginName)) {
                    plugin.logger.log(Level.WARNING, "$pluginName is required but not installed!")
                    Bukkit.getPluginManager().disablePlugin(plugin)
                }
            }
        }
    }
}