@file:Suppress("unused")

package me.matin.mcore.managers.plugin

import kotlinx.coroutines.*
import me.matin.mcore.mcore
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume


@OptIn(InternalCoroutinesApi::class)
object MainBukkitDispatcher : CoroutineDispatcher(), Delay {
	
	private lateinit var dispatcher: CoroutineDispatcher
	private lateinit var plugin: Plugin
	
	val immediate: CoroutineDispatcher
		get() = object : CoroutineDispatcher(), Delay {
			
			override fun dispatch(context: CoroutineContext, block: Runnable) = dispatcher.dispatch(context, block)
			
			override fun isDispatchNeeded(context: CoroutineContext) = !Bukkit.isPrimaryThread()
			
			override fun scheduleResumeAfterDelay(
				timeMillis: Long,
				continuation: CancellableContinuation<Unit>
			) = this@MainBukkitDispatcher.scheduleResumeAfterDelay(timeMillis, continuation)
		}
	
	override fun dispatch(context: CoroutineContext, block: Runnable) = dispatcher.dispatch(context, block)
	
	override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
		val ticks = (timeMillis / 50).coerceAtLeast(1)
		Bukkit.getScheduler()
			.runTaskLater(plugin, Runnable { continuation.resume(Unit) }, ticks)
			.run { continuation.invokeOnCancellation { cancel() } }
	}
	
	context(plugin: Plugin)
	internal fun init() {
		this.plugin = plugin
		dispatcher = Bukkit.getScheduler().getMainThreadExecutor(plugin).asCoroutineDispatcher()
	}
	
	internal fun close() {
		immediate.cancel()
		cancel()
	}
}

suspend fun tickDelay(timeTicks: Long) = suspendCancellableCoroutine { continuation ->
	Bukkit.getScheduler()
		.runTaskLater(mcore.getOrThrow(), Runnable { continuation.resume(Unit) }, timeTicks)
		.run { continuation.invokeOnCancellation { cancel() } }
}
