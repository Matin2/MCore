@file:Suppress("unused", "NOTHING_TO_INLINE")

package me.matin.mcore.methods

import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.time.Duration
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

/** Converts this [Duration] to server ticks. */
inline val Duration.inTicks: Double get() = toDouble(SECONDS) * 20

/** Converts this [Duration] to server ticks. */
inline val Duration.inWholeTicks: Long get() = inTicks.toLong()

/** Converts this number to server ticks ([Duration]). */
inline val <T: Number> T.ticks: Duration get() = toDouble().ticks

/** Converts this double to server ticks ([Duration]). */
inline val Double.ticks: Duration get() = div(20).toDuration(SECONDS)

inline fun Plugin.registerListeners(vararg listeners: Listener) = listeners.forEach {
	server.pluginManager.registerEvents(it, this)
}

inline var Plugin.enabled: Boolean
	get() = isEnabled
	set(value) = when (value) {
		isEnabled -> Unit
		true -> server.pluginManager.enablePlugin(this)
		false -> server.pluginManager.disablePlugin(this)
	}

inline fun ItemStack?.checkEmpty(): ItemStack? = takeUnless { it?.isEmpty == true }