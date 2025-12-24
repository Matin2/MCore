package me.matin.mcore.managers.plugin

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.matin.mcore.managers.hook.HooksHandler
import me.matin.mcore.managers.hook.HooksManager
import org.bukkit.plugin.java.JavaPlugin
import kotlin.coroutines.CoroutineContext

abstract class KotlinPlugin: JavaPlugin(), CoroutineScope {
	
	val dispatchers = BukkitDispatchers(this)
	val hooksHandler = HooksHandler(this).also { HooksManager += it }
	internal val job = SupervisorJob()
	override val coroutineContext: CoroutineContext = CoroutineName(name) + job + dispatchers.main
	
	override fun onEnable() {
		hooksHandler.onEnable()
	}
	
	override fun onDisable() {
		hooksHandler.onDisable()
		val exception = CancellationException("Plugin has been disabled.")
		job.cancel(exception)
		dispatchers.cancel(exception)
	}
}