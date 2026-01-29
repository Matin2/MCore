@file:Suppress("unused")

package me.matin.mcore.managers.plugin

import kotlinx.coroutines.*
import me.matin.mcore.mcore
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume


@OptIn(InternalCoroutinesApi::class)
object MainBukkitDispatcher : CoroutineDispatcher(), Delay {
	
	internal lateinit var dispatcher: CoroutineDispatcher
	
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
			.runTaskLater(mcore.getOrThrow(), Runnable { continuation.resume(Unit) }, ticks)
			.run { continuation.invokeOnCancellation { cancel() } }
	}
}

suspend fun delayTicks(ticks: Long) = suspendCancellableCoroutine { cont ->
	Bukkit.getScheduler()
		.runTaskLater(mcore.getOrThrow(), Runnable { cont.resume(Unit) }, ticks)
		.run { cont.invokeOnCancellation { cancel() } }
}
