package com.github.matin2.mcore.services.plugin

import com.github.matin2.mcore.services.KoinService
import kotlinx.coroutines.*
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.dsl.KoinConfiguration

abstract class KotlinPlugin : JavaPlugin(), CoroutineScope, KoinComponent {
	
	override val coroutineContext = CoroutineName("Plugin #$name") + SupervisorJob() + Dispatchers.Bukkit
	
	open val koinConfig: KoinConfiguration? = null
	private lateinit var koin: Koin
	
	override fun getKoin() = koin
	
	override fun onEnable() {
		koin = KoinService.new(this, this::class, koinConfig ?: return)
	}
	
	override fun onDisable() {
		if (::koin.isInitialized) KoinService.close(this::class)
		cancel("$name has been disabled.")
	}
}
