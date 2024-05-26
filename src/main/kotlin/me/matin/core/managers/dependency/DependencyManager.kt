package me.matin.core.managers.dependency

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
        val statues = ArrayList<Boolean>()
        pluginNames.forEach {
            val plugin = Bukkit.getPluginManager().getPlugin(it)
            statues.add(plugin != null && plugin.isEnabled)
        }
        return statues.all { it }
    }

    @JvmStatic
    fun checkDepends(plugins: Set<String>, registerListener: Plugin? = null, action: (name: String, installed: Boolean) -> Unit) {
        plugins.forEach {
            action(it, isPluginInstalled(it))
        }
        registerListener?.also {
            Bukkit.getPluginManager().registerEvents(DependencyListener(plugins, action), it)
        }
    }

    @JvmStatic
    fun checkDepends(pluginToVersions: Map<String, String>, registerListener: Plugin? = null, action: (name: String, state: DependencyState) -> Unit) {
        for (name in pluginToVersions.keys) {
            val versions = pluginToVersions[name]!!
            if (versions.isBlank()) {
                action(name, if (isPluginInstalled(name)) DependencyState.INSTALLED else DependencyState.NOT_INSTALLED)
                continue
            }
            if (!isPluginInstalled(name)) {
                action(name, DependencyState.NOT_INSTALLED)
                continue
            }
            action(name, Bukkit.getPluginManager().getPlugin(name)!!.checkVersions(versions))
        }
        registerListener?.also {
            Bukkit.getPluginManager().registerEvents(DependencyListener(pluginToVersions, action), it)
        }
    }

    internal fun Plugin.checkVersions(versions: String): DependencyState {
        val version = this.pluginMeta.version.uppercase()
        val status = ArrayList<Boolean>()
        versions.split('\\').forEach {
            it.uppercase().apply {
                when {
                    startsWith('*') -> status.add(version.contains(removePrefix("*")))
                    startsWith('>') -> status.add(version.startsWith(removePrefix(">")))
                    startsWith('<') -> status.add(version.endsWith(removePrefix("<")))
                    startsWith("!*") -> status.add(!version.contains(removePrefix("!*")))
                    startsWith("!>") -> status.add(version.startsWith(removePrefix("!>")))
                    startsWith("!<") -> status.add(version.endsWith(removePrefix("!<")))
                    startsWith('!') -> status.add(version != removePrefix("!"))
                    else -> status.add(version == this)
                }
            }
        }
        return if (status.any { it }) DependencyState.INSTALLED else DependencyState.WRONG_VERSION
    }
}