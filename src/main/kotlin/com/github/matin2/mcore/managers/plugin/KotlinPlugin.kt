package com.github.matin2.mcore.managers.plugin

import com.github.matin2.mcore.MCore
import com.github.matin2.mcore.managers.hook.HooksHandler
import com.github.retrooper.packetevents.PacketEventsAPI
import kotlinx.coroutines.*
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.dsl.onClose

abstract class KotlinPlugin : JavaPlugin(), CoroutineScope, KoinComponent {
	
	override val coroutineContext = CoroutineName(name) + SupervisorJob() + Dispatchers.Default
	
	private lateinit var koinApp: KoinApplication
	
	override fun getKoin() = koinApp.koin
	
	fun enableKoin(vararg modules: Module) {
		val internal = module {
			single<KotlinPlugin> { this@KotlinPlugin }
			single<PacketEventsAPI<*>> { requireNotNull(MCore.packetEventsAPI) }
			single<HooksHandler> { HooksHandler(this@KotlinPlugin) } onClose { it?.close() }
		}
		koinApp = koinApplication { modules(*modules, internal) }
		koins[name] = koinApp.koin
	}
	
	override fun onDisable() {
		if (::koinApp.isInitialized) {
			koinApp.close()
			koins.remove(name)
		}
		cancel("$name has been disabled.")
	}
}

private val koins = mutableMapOf<String, Koin>()

fun pluginKoin(pluginName: String) = koins.getValue(pluginName)
