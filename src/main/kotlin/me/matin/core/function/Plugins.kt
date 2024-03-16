package me.matin.core.function

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class Plugins {

    fun hasPlugin(pluginName: String): Boolean {
        val plugin = Bukkit.getPluginManager().getPlugin(pluginName)
        return plugin != null && plugin.isEnabled
    }

    fun hasPlugins(pluginNames: String): Boolean {
        val pluginNames1: Array<String> = pluginNames.split(',').toTypedArray()
        return hasPlugins(pluginNames1)
    }

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

    fun getPlugin(pluginName: String): Plugin? {
        return Bukkit.getPluginManager().getPlugin(pluginName)
    }

    fun checkDepends(plugin: Plugin, depends: Array<String>) {
        for (pluginName in depends) {
            if (!hasPlugin(pluginName)) {
                println("$pluginName is required but not installed, plugin disabled.")
                Bukkit.getPluginManager().disablePlugin(plugin)
            }
        }
    }
}