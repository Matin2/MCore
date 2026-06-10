@file:Suppress("unused")

package me.matin.mcore.managers.plugin

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

@Suppress("UnusedReceiverParameter")
val Dispatchers.Bukkit: CoroutineDispatcher get() = BukkitDispatcher

@OptIn(InternalCoroutinesApi::class)
internal object BukkitDispatcher : CoroutineDispatcher(), Delay {
	
	private lateinit var executor: Executor
	private lateinit var plugin: Plugin
	
	override fun isDispatchNeeded(context: CoroutineContext) = !Bukkit.isPrimaryThread()
	
	override fun dispatch(context: CoroutineContext, block: Runnable) = executor.execute(block)
	
	override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
		val task = Bukkit.getScheduler().runTaskLater(
			plugin,
			Runnable { continuation.resume(Unit) },
			(timeMillis / 50).coerceAtLeast(1L)
		)
		continuation.invokeOnCancellation { task.cancel() }
	}
	
	override fun invokeOnTimeout(
		timeMillis: Long,
		block: Runnable,
		context: CoroutineContext
	): DisposableHandle {
		val task = Bukkit.getScheduler().runTaskLater(
			plugin,
			block,
			(timeMillis / 50).coerceAtLeast(1L)
		)
		return DisposableHandle { task.cancel() }
	}
	
	fun Plugin.initBukkitDispatcher() {
		plugin = this
		executor = Bukkit.getScheduler().getMainThreadExecutor(this)
	}
}
