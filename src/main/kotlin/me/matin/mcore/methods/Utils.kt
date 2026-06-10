@file:Suppress("unused", "NOTHING_TO_INLINE")

package me.matin.mcore.methods

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import kotlin.time.Duration
import kotlin.time.toDuration

/** Returns a [Duration] equal to this [Int] number of server ticks. */
inline val Int.ticks get() = times(50).toDuration(MILLISECONDS)

/** Returns a [Duration] equal to this [Long] number of server ticks. */
inline val Long.ticks get() = times(50).toDuration(MILLISECONDS)

/**
 * The value of this duration expressed as a [Long] number of server ticks.
 *
 * The part of this duration that is smaller than a server tick becomes a
 * fractional part of the result and then is truncated (rounded towards
 * zero).
 *
 * An infinite duration value is converted either to [Long.MAX_VALUE] or
 * [Long.MIN_VALUE] depending on its sign.
 */
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
