package me.matin.mcore.managers.plugin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import me.matin.mcore.mcore
import me.matin.mcore.methods.inTicks
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.time.Duration


object MainBukkitDispatcher : CoroutineDispatcher() {
	
	private val dispatcher: CoroutineDispatcher? =
		Bukkit.getScheduler().getMainThreadExecutor(mcore).asCoroutineDispatcher()
		get() = runCatching { field }.getOrNull()
	
	override fun dispatch(context: CoroutineContext, block: Runnable) = dispatcher?.dispatch(context, block) ?: Unit
	
	override fun isDispatchNeeded(context: CoroutineContext) = !Bukkit.isPrimaryThread()
}

suspend fun delayTicks(ticks: Long) = suspendCancellableCoroutine { cont ->
	Bukkit.getScheduler().runTaskLater(mcore, Runnable { cont.resume(Unit) }, ticks).run {
		cont.invokeOnCancellation { cancel() }
	}
}

@Suppress("unused")
suspend inline fun delayTicks(duration: Duration) = delayTicks(duration.inTicks)
