package com.github.matin2.mcore.methods.utils.event_flows

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
fun <E : Event> Plugin.eventFlow(event: Class<E>, priority: EventPriority, ignoreCancelled: Boolean): Flow<E> =
	callbackFlow {
		val listener = object : Listener {}
		val executor = EventExecutor { _, e -> trySend(event.cast(e)) }
		Bukkit.getPluginManager().registerEvent(
			event,
			listener,
			priority,
			executor,
			this@eventFlow,
			ignoreCancelled
		)
		awaitClose { HandlerList.unregisterAll(listener) }
	}

@Suppress("unused")
inline fun <reified E : Event> Plugin.eventFlow(
	eventPriority: EventPriority = NORMAL,
	ignoreCancelled: Boolean = false
) = eventFlow(E::class.java, eventPriority, ignoreCancelled)
