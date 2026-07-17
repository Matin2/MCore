package com.github.matin2.mcore.methods.utils.event_flows

import com.github.matin2.mcore.managers.plugin.KotlinPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.shareIn
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.plugin.Plugin

inline fun <reified E : Event> CoroutineScope.sharedEventFlow(
	plugin: Plugin,
	priority: EventPriority = NORMAL,
	ignoreCancelled: Boolean = false,
	replay: Int = 1,
	start: SharingStarted = Eagerly,
	buffer: Int = BUFFERED,
	onBufferOverflow: BufferOverflow = DROP_OLDEST
) = plugin.eventFlow<E>(priority, ignoreCancelled)
	.buffer(buffer, onBufferOverflow)
	.shareIn(this, start, replay)

@Suppress("unused")
inline fun <reified E : Event> KotlinPlugin.sharedEventFlow(
	priority: EventPriority = NORMAL,
	ignoreCancelled: Boolean = false,
	replay: Int = 1,
	start: SharingStarted = Eagerly,
	buffer: Int = BUFFERED,
	onBufferOverflow: BufferOverflow = DROP_OLDEST
) = sharedEventFlow<E>(
	this,
	priority,
	ignoreCancelled,
	replay,
	start,
	buffer,
	onBufferOverflow
)
