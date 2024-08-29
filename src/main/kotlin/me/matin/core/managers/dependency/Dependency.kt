@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package me.matin.core.managers.dependency

import me.matin.core.Core
import org.bukkit.Bukkit
import java.util.logging.Logger

class Dependency(
    val name: String,
    private val versionPredicate: (version: String) -> Boolean
) {

    val state get() = DependencyState[name, versionPredicate]

    constructor(name: String, vararg versions: String): this(name, { it in versions })
    constructor(name: String): this(name, { true })

    operator fun component1() = name
    operator fun component2() = state

    fun use(block: DependencyComponent.() -> Unit) {
        DependencyComponent(state, Core.instance.logger, false).block()
        DependencyListener.monitoredPlugins[this] = block
    }
}

data class DependencyComponent(val state: DependencyState, val logger: Logger, val stateChanged: Boolean)

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
