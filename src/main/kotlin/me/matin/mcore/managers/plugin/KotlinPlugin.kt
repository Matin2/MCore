package me.matin.mcore.managers.plugin

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.matin.mcore.managers.hook.HooksHandler
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

abstract class KotlinPlugin : JavaPlugin(), CoroutineScope {
	
	private val job = SupervisorJob()
	private lateinit var _hooksHandler: Lazy<HooksHandler>
	val hooksHandler by _hooksHandler
	lateinit var dispatchers: BukkitDispatchers private set
	override val coroutineContext get() = CoroutineName(name) + job + BukkitDispatchers.Main(this)
	
	override fun onEnable() {
		dispatchers = BukkitDispatchers(this)
		_hooksHandler = lazy { HooksHandler(this).also(HooksHandler::init) }
	}
	
	override fun onDisable() {
		val exception = CancellationException("Plugin has been disabled.")
		job.cancel(exception)
		dispatchers.cancel(exception)
		Bukkit.getScheduler().cancelTasks(this)
		if (_hooksHandler.isInitialized()) hooksHandler.disable()
	}
}