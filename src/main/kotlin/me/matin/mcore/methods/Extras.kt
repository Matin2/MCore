@file:Suppress("unused")

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

fun Plugin.registerListeners(vararg listeners: Listener) = listeners.forEach {
	server.pluginManager.registerEvents(it, this)
}

fun ItemStack?.checkEmpty(): ItemStack? = takeUnless { it?.isEmpty == true }