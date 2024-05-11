package me.matin.core.managers

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage", "LocalVariableName", "unused")
object DependencyManager {

    @JvmStatic
    fun isPluginInstalled(pluginName: String): Boolean {
        val plugin = Bukkit.getPluginManager().getPlugin(pluginName)
        return plugin != null && plugin.isEnabled
    }

    @JvmStatic
    fun arePluginsInstalled(pluginNames: Array<String>): Boolean {
        val statues = ArrayList<Boolean>()
        pluginNames.forEach {
            val plugin = Bukkit.getPluginManager().getPlugin(it.trim())
            statues.add(plugin != null && plugin.isEnabled)
        }
        return statues.all { it }
    }

    @JvmStatic
    fun checkDepends(plugins: Set<String>): Map<String, Boolean> {
        val map = HashMap<String, Boolean>()
        plugins.forEach {
            map[it] = isPluginInstalled(it)
        }
        return map
    }

    @JvmStatic
    fun checkDepends(plugins: Set<String>, function: (String, Boolean) -> Unit) {
        plugins.forEach {
            function(it.trim(), isPluginInstalled(it))
        }
    }

    @JvmStatic
    fun checkDepends(plugin_versions: Map<String, String>): Map<String, Boolean> {
        val map = HashMap<String, Boolean>()
        for (name in plugin_versions.keys) {
            if (!isPluginInstalled(name)) {
                map[name.trim()] = false
                continue
            }
            map[name.trim()] = checkVersions(Bukkit.getPluginManager().getPlugin(name)!!, plugin_versions[name]!!)
        }
        return map
    }

    @JvmStatic
    fun checkDepends(plugin_versions: Map<String, String>, function: (String, Boolean) -> Unit) {
        for (name in plugin_versions.keys) {
            if (!isPluginInstalled(name)) {
                function(name.trim(), false)
                continue
            }
            function(name.trim(), checkVersions(Bukkit.getPluginManager().getPlugin(name)!!, plugin_versions[name]!!))
        }
    }

    private fun checkVersions(plugin: Plugin, versions: String): Boolean {
        val version = plugin.pluginMeta.version.uppercase()
        val map = HashMap<String, Boolean>()
        versions.split('\\').filter { it.isNotBlank() }.forEach {
            val ver = it.trim().uppercase()
            if (ver.startsWith('*')) map[ver] = version.contains(ver.trimStart('*'))
            else if (ver.startsWith("!*")) map[ver] = !version.contains(ver.trimStart('!', '*'))
            else if (ver.startsWith('!')) map[ver] = version != ver.trimStart('!')
            else map[ver] = version == ver
        }
        return map.values.any { it }
    }
}