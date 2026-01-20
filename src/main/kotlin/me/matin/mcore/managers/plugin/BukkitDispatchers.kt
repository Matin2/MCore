@file:Suppress("unused")

package me.matin.mcore.managers.plugin

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class BukkitDispatchers(private val plugin: Plugin) {
	
	private val _main = lazy { Main(plugin) }
	private val _async = lazy { Async(plugin) }
	val main by _main
	val async by _async
	
	fun cancel(exception: CancellationException? = null) {
		if (_main.isInitialized()) main.cancel(exception)
		if (_async.isInitialized()) async.cancel(exception)
	}
	
	class Main(private val plugin: Plugin) : CoroutineDispatcher() {
		
		override fun dispatch(context: CoroutineContext, block: Runnable) {
			if (Bukkit.isPrimaryThread()) block.run()
			else Bukkit.getScheduler().runTask(plugin, block)
		}
	}
	
	class Async(private val plugin: Plugin) : CoroutineDispatcher() {
		
		override fun dispatch(context: CoroutineContext, block: Runnable) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, block)
		}
	}
}

suspend fun delayTicks(plugin: Plugin, delay: Long) = suspendCancellableCoroutine { cont ->
	Bukkit.getScheduler().runTaskLater(plugin, Runnable { cont.resume(Unit) }, delay).run {
		cont.invokeOnCancellation { cancel() }
	}
}

suspend fun delayTicksAsync(plugin: Plugin, delay: Long) = suspendCancellableCoroutine { cont ->
	Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable { cont.resume(Unit) }, delay).run {
		cont.invokeOnCancellation { cancel() }
	}
}
