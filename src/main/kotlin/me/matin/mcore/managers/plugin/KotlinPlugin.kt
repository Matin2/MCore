package me.matin.mcore.managers.plugin

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.bukkit.plugin.java.JavaPlugin
import kotlin.coroutines.CoroutineContext

abstract class KotlinPlugin: JavaPlugin(), CoroutineScope {
	
	private val _dispatcher = lazy { BukkitDispatcher(this) }
	val dispatcher by _dispatcher
	internal val job = SupervisorJob()
	override val coroutineContext: CoroutineContext = CoroutineName(name) + job + BukkitDispatcher(this).main
	
	override fun onDisable() {
		val exception = CancellationException("Plugin has been disabled.")
		job.cancel(exception)
		if (_dispatcher.isInitialized()) dispatcher.cancel(exception)
	}
}