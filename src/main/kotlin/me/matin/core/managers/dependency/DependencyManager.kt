package me.matin.core.managers.dependency

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
    fun checkDepends(plugins: Set<String>): Map<String, CheckedDepend> {
        val map = HashMap<String, CheckedDepend>()
        plugins.forEach {
            map[it] = if (isPluginInstalled(it)) CheckedDepend.INSTALLED else CheckedDepend.NOT_INSTALLED
        }
        return map
    }

    @JvmStatic
    fun checkDepends(plugins: Set<String>, action: (String, CheckedDepend) -> Unit, registerListener: Plugin? = null) {
        plugins.forEach {
            action(it, if (isPluginInstalled(it)) CheckedDepend.INSTALLED else CheckedDepend.NOT_INSTALLED)
        }
        if (registerListener != null) Bukkit.getPluginManager().registerEvents(DependencyListener(plugins, action), registerListener)
    }

    @JvmStatic
    fun checkDepends(plugin_versions: Map<String, String>): Map<String, CheckedDepend> {
        val map = HashMap<String, CheckedDepend>()
        for (name in plugin_versions.keys) {
            val versions = plugin_versions[name]!!
            if (versions.isBlank()) {
                map[name] = if (isPluginInstalled(name)) CheckedDepend.INSTALLED else CheckedDepend.NOT_INSTALLED
                continue
            }
            if (!isPluginInstalled(name)) {
                map[name] = CheckedDepend.NOT_INSTALLED
                continue
            }
            map[name] = if (Bukkit.getPluginManager().getPlugin(name)!!.checkVersions(versions))
                CheckedDepend.INSTALLED else CheckedDepend.WRONG_VERSION
        }
        return map
    }

    @JvmStatic
    fun checkDepends(plugin_versions: Map<String, String>, action: (String, CheckedDepend) -> Unit, registerListener: Plugin? = null) {
        for (name in plugin_versions.keys) {
            val versions = plugin_versions[name]!!
            if (versions.isBlank()) {
                action(name, if (isPluginInstalled(name)) CheckedDepend.INSTALLED else CheckedDepend.NOT_INSTALLED)
                continue
            }
            if (!isPluginInstalled(name)) {
                action(name, CheckedDepend.NOT_INSTALLED)
                continue
            }
            action(name, if (Bukkit.getPluginManager().getPlugin(name)!!.checkVersions(versions))
                CheckedDepend.INSTALLED else CheckedDepend.WRONG_VERSION)
        }
        if (registerListener != null) Bukkit.getPluginManager().registerEvents(DependencyListener(plugin_versions, action), registerListener)
    }

    internal fun Plugin.checkVersions(versions: String): Boolean {
        val version = this.pluginMeta.version.uppercase()
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