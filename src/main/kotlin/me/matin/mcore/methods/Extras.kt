@file:Suppress("unused", "NOTHING_TO_INLINE")

package me.matin.mcore.methods

import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.time.Duration

/** Converts this [Duration] to server ticks. */
inline val Duration.inTicks: Double get() = toDouble(SECONDS) * 20

/** Converts this [Duration] to server ticks. */
inline val Duration.inWholeTicks: Long get() = inTicks.toLong()

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

inline fun ItemStack.checkEmpty(): ItemStack? = takeUnless { it.isEmpty }
