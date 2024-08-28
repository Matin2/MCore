@file:Suppress("unused")

package me.matin.core.managers.dependency

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

typealias StateChangeAction = Plugin.(newState: DependencyState) -> Unit

data class Dependency(val name: String, private val versionPredicate: (version: String) -> Boolean) {

    val state get() = DependencyState[name, versionPredicate]

    constructor(name: String, vararg versions: String): this(name, { it in versions })
    constructor(name: String): this(name, { true })

    infix fun onStateChange(block: StateChangeAction) {
        DependencyListener.monitoredPlugins[this] = block
    }
}

sealed class DependencyState(val boolean: Boolean) {

    data object AVAILABLE: DependencyState(true)

    data object UNAVAILABLE: DependencyState(false) {

        internal var isWrongVersion = false
        val wrongVersion: Boolean get() = isWrongVersion
    }

    internal companion object {

        @Suppress("UnstableApiUsage")
        operator fun get(name: String, versionPredicate: (String) -> Boolean): DependencyState =
            Bukkit.getPluginManager().getPlugin(name)?.takeIf { it.isEnabled }?.run {
                if (versionPredicate(pluginMeta.version)) AVAILABLE else UNAVAILABLE.apply { isWrongVersion = true }
            } ?: UNAVAILABLE
    }
}
