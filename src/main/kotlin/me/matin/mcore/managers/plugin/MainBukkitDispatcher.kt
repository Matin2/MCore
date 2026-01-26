package me.matin.mcore.managers.plugin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.suspendCancellableCoroutine
import me.matin.mcore.mcore
import me.matin.mcore.methods.inTicks
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.time.Duration


object MainBukkitDispatcher : CoroutineDispatcher() {
	
	internal lateinit var dispatcher: CoroutineDispatcher
	
	override fun dispatch(context: CoroutineContext, block: Runnable) = dispatcher.dispatch(context, block)
	
	override fun isDispatchNeeded(context: CoroutineContext) = !Bukkit.isPrimaryThread()
}

suspend fun delayTicks(ticks: Long) = suspendCancellableCoroutine { cont ->
	Bukkit.getScheduler()
		.runTaskLater(mcore.getOrThrow(), Runnable { cont.resume(Unit) }, ticks)
		.run { cont.invokeOnCancellation { cancel() } }
}

@Suppress("unused")
suspend inline fun delayTicks(duration: Duration) = delayTicks(duration.inTicks)
