@file:Suppress("unused")

package com.github.matin2.mcore.methods.utils

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

/**
 * Registers all the events in all the given listener classes.
 *
 * @param listeners Listeners to register
 * @receiver Plugin to register
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Plugin.registerListeners(vararg listeners: Listener) = listeners.forEach {
	server.pluginManager.registerEvents(it, this)
}

/**
 * Returns a [Boolean] indicating whether this plugin is currently enabled
 * and allows you to enable or disable it.
 */
inline var Plugin.enabled: Boolean
	get() = isEnabled
	set(value) = when (value) {
		isEnabled -> Unit
		true -> server.pluginManager.enablePlugin(this)
		false -> server.pluginManager.disablePlugin(this)
	}
