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
            val plugin = Bukkit.getPluginManager().getPlugin(it)
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
    fun checkDepends(plugins: Set<String>, forEach: (String, Boolean) -> Unit) {
        plugins.forEach {
            forEach(it, isPluginInstalled(it))
        }
    }

    @JvmStatic
    fun checkDepends(plugin_versions: Map<String, String>): Map<String, Boolean> {
        val map = HashMap<String, Boolean>()
        for (name in plugin_versions.keys) {
            if (!isPluginInstalled(name)) {
                map[name] = false
                continue
            }
            map[name] = checkVersions(Bukkit.getPluginManager().getPlugin(name)!!, plugin_versions[name]!!)
        }
        return map
    }

    @JvmStatic
    fun checkDepends(plugin_versions: Map<String, String>, forEach: (String, Boolean) -> Unit) {
        for (name in plugin_versions.keys) {
            if (!isPluginInstalled(name)) {
                forEach(name.trim(), false)
                continue
            }
            forEach(name, checkVersions(Bukkit.getPluginManager().getPlugin(name)!!, plugin_versions[name]!!))
        }
    }

    private fun checkVersions(plugin: Plugin, versions: String): Boolean {
        val version = plugin.pluginMeta.version.uppercase()
        val status = ArrayList<Boolean>()
        versions.split('\\').forEach {
            val ver = it.uppercase()
            with (ver) { when {
                startsWith('*') -> status.add(version.contains(ver.removePrefix("*")))
                startsWith('>') -> status.add(version.startsWith(ver.removePrefix(">")))
                startsWith('<') -> status.add(version.endsWith(ver.removePrefix("<")))
                startsWith("!*") -> status.add(!version.contains(ver.removePrefix("!*")))
                startsWith("!>") -> status.add(version.startsWith(ver.removePrefix("!>")))
                startsWith("!<") -> status.add(version.endsWith(ver.removePrefix("!<")))
                startsWith('!') -> status.add(version != ver.removePrefix("!"))
                else -> status.add(version == ver)
            } }
        }
        return status.any { it }
    }
}