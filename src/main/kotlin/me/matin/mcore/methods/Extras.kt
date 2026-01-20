@file:Suppress("unused", "NOTHING_TO_INLINE")

package me.matin.mcore.methods

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import kotlin.time.Duration

/** Converts this [Duration] to server ticks. */
inline val Duration.inTicks get() = toLong(MILLISECONDS) / 50

/**
 * Registers all the events in all the given listener classes.
 *
 * @param listeners Listeners to register
 * @receiver Plugin to register
 */
inline fun Plugin.registerListeners(vararg listeners: Listener) = listeners.forEach {
	server.pluginManager.registerEvents(it, this)
}

/**
 * Returns a value indicating whether this plugin is currently enabled and
 * allows you to enable or disable it.
 *
 * @receiver Plugin to evaluate state of
 */
inline var Plugin.enabled: Boolean
	get() = isEnabled
	set(value) = when (value) {
		isEnabled -> Unit
		true -> server.pluginManager.enablePlugin(this)
		false -> server.pluginManager.disablePlugin(this)
	}

inline infix fun <A, B, C> Pair<A, B>.and(third: C) = Triple(first, second, third)