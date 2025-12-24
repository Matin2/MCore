@file:Suppress("unused")

package me.matin.mcore.managers.plugin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

class BukkitDispatchers internal constructor(private val plugin: Plugin) {
	
	val main = object: CoroutineDispatcher() {
		override fun dispatch(context: CoroutineContext, block: Runnable) {
			if (Bukkit.isPrimaryThread()) {
				block.run()
				return
			}
			Bukkit.getScheduler().runTask(plugin, block)
		}
	}
	val async = object: CoroutineDispatcher() {
		override fun dispatch(context: CoroutineContext, block: Runnable) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, block)
		}
	}
	
	internal fun cancel(exception: CancellationException) {
		main.cancel(exception)
		async.cancel(exception)
	}
}

suspend fun Plugin.delayTicks(delay: Long) = suspendCancellableCoroutine { cont ->
	val task = Bukkit.getScheduler().runTaskLater(this, Runnable { cont.resume(Unit) }, delay)
	cont.invokeOnCancellation { task.cancel() }
}

suspend inline fun delayTicks(plugin: Plugin, delay: Long) = plugin.delayTicks(delay)
