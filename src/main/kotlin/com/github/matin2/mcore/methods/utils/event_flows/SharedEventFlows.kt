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
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
fun <E : Event> Plugin.sharedEventFlow(
	scope: CoroutineScope,
	event: Class<E>,
	eventPriority: EventPriority,
	ignoreCancelledEvents: Boolean,
	replay: Int,
	bufferCapacity: Int,
	onBufferOverflow: BufferOverflow,
	sharingStarted: SharingStarted
) = eventFlow(event, eventPriority, ignoreCancelledEvents)
	.buffer(bufferCapacity, onBufferOverflow)
	.shareIn(scope, sharingStarted, replay)


inline fun <reified E : Event> Plugin.sharedEventFlow(
	scope: CoroutineScope,
	eventPriority: EventPriority = NORMAL,
	ignoreCancelledEvents: Boolean = false,
	replay: Int = 0,
	bufferCapacity: Int = BUFFERED,
	onBufferOverflow: BufferOverflow = SUSPEND,
	sharingStarted: SharingStarted = Eagerly
) = sharedEventFlow(
	scope,
	E::class.java,
	eventPriority,
	ignoreCancelledEvents,
	replay,
	bufferCapacity,
	onBufferOverflow,
	sharingStarted
)

@Suppress("unused")
inline fun <reified E : Event> KotlinPlugin.sharedEventFlow(
	eventPriority: EventPriority = NORMAL,
	ignoreCancelledEvents: Boolean = false,
	replay: Int = 0,
	bufferCapacity: Int = BUFFERED,
	onBufferOverflow: BufferOverflow = SUSPEND,
	sharingStarted: SharingStarted = Eagerly
) = sharedEventFlow<E>(
	this,
	eventPriority,
	ignoreCancelledEvents,
	replay,
	bufferCapacity,
	onBufferOverflow,
	sharingStarted
)
