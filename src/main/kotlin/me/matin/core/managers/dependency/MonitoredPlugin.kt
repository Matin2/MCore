package me.matin.core.managers.dependency

import org.bukkit.plugin.Plugin

class MonitoredPlugin(private val dependencies: Map<String, String>, private val action: Plugin.(name: String, state: DependencyState) -> Unit) {

    operator fun component1(): Map<String, String> = dependencies
    operator fun component2(): Plugin.(String, DependencyState) -> Unit = action

    constructor (dependencies: Set<String>, action: Plugin.(name: String, installed: Boolean) -> Unit): this(dependencies.let { depends ->
        val map = emptyMap<String, String>() as MutableMap
        depends.forEach { map[it] = "" }
        map
    }, { name, state -> action(name, state.value) })
}