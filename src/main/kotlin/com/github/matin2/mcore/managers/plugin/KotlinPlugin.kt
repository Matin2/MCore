package com.github.matin2.mcore.managers.plugin

import com.github.matin2.mcore.managers.hook.HooksHandler
import kotlinx.coroutines.*
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.koinApplication
import org.koin.dsl.module

abstract class KotlinPlugin : JavaPlugin(), CoroutineScope, KoinComponent {
	
	override val coroutineContext = CoroutineName(name) + SupervisorJob() + Dispatchers.Default
	
	private lateinit var koinApp: KoinApplication
	private val _hooksHandler = lazy { HooksHandler(this) }
	val hooksHandler by _hooksHandler
	
	override fun getKoin() = koinApp.koin
	
	fun enableKoin(vararg modules: Module) {
		val internal = module {
			single { this@KotlinPlugin } binds [KotlinPlugin::class, this@KotlinPlugin::class]
			single { hooksHandler }
		}
		koinApp = koinApplication { modules(*modules, internal) }
		koins[name] = koinApp.koin
	}
	
	override fun onDisable() {
		if (_hooksHandler.isInitialized()) hooksHandler.close()
		if (::koinApp.isInitialized) {
			koinApp.close()
			koins.remove(name)
		}
		cancel("$name has been disabled.")
	}
	
	companion object {
		private val koins = mutableMapOf<String, Koin>()
		fun getKoin(plugin: String) = koins.getValue(plugin)
	}
}
