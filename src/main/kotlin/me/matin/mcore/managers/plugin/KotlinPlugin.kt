package me.matin.mcore.managers.plugin

import kotlinx.coroutines.*
import me.matin.mcore.managers.hook.HooksHandler
import org.bukkit.plugin.java.JavaPlugin
import kotlin.properties.ReadOnlyProperty

abstract class KotlinPlugin : JavaPlugin() {
	
	private lateinit var _hooksHandler: Lazy<HooksHandler>
	private lateinit var _lifecycleScope: CoroutineScope
	val lifecycleScope: CoroutineScope get() = _lifecycleScope
	val hooksHandler by _hooksHandler
	
	override fun onEnable() {
		_lifecycleScope = CoroutineScope(CoroutineName(name) + SupervisorJob() + Dispatchers.Default)
		_hooksHandler = lazy { HooksHandler(this).also(HooksHandler::init) }
		plugins += this
	}
	
	override fun onDisable() {
		_lifecycleScope.cancel(CancellationException("Plugin has been disabled."))
		if (_hooksHandler.isInitialized()) hooksHandler.disable()
		plugins -= this
	}
	
	companion object {
		
		@JvmStatic
		val plugins: List<KotlinPlugin>
			field : MutableList<KotlinPlugin> = mutableListOf()
		
		@JvmStatic
		inline fun <reified P : KotlinPlugin> get() = ReadOnlyProperty<Any?, Result<P>> { _, _ ->
			runCatching { plugins.filterIsInstance<P>().first() }
		}
	}
}