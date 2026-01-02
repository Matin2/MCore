package me.matin.mcore.managers.plugin

import kotlinx.coroutines.*
import me.matin.mcore.managers.hook.HooksHandler
import org.bukkit.plugin.java.JavaPlugin

abstract class KotlinPlugin: JavaPlugin() {
	
	lateinit var scope: CoroutineScope private set
	lateinit var dispatchers: BukkitDispatchers private set
	val hooksHandler by _hooksHandler
	private lateinit var _hooksHandler: Lazy<HooksHandler>
	
	override fun onEnable() {
		dispatchers = BukkitDispatchers(this)
		scope = CoroutineScope(CoroutineName(name) + SupervisorJob() + dispatchers.main)
		_hooksHandler = lazy { HooksHandler(this).also(HooksHandler::init) }
	}
	
	override fun onDisable() {
		scope.cancel(CancellationException("Plugin has been disabled."))
		if (_hooksHandler.isInitialized()) hooksHandler.disable()
	}
}