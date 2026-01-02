@file:Suppress("unused")

package me.matin.mcore.managers.plugin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class BukkitDispatchers(private val plugin: Plugin) {
	
	val main by lazy { Main(plugin) }
	val async by lazy { Async(plugin) }
	
	class Main(private val plugin: Plugin): CoroutineDispatcher() {
		
		override fun dispatch(context: CoroutineContext, block: Runnable) {
			if (Bukkit.isPrimaryThread()) block.run()
			else Bukkit.getScheduler().runTask(plugin, block)
		}
	}
	
	class Async(private val plugin: Plugin): CoroutineDispatcher() {
		
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
