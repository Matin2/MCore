package com.github.matin2.mcore.services.plugin

import com.github.matin2.mcore.MCore
import com.github.matin2.mcore.services.KoinService
import kotlinx.coroutines.*
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.dsl.KoinConfiguration

abstract class KotlinPlugin : JavaPlugin(), CoroutineScope, KoinComponent {
	
	override val coroutineContext = CoroutineName(name) + SupervisorJob() + Dispatchers.Bukkit
	
	open lateinit var koinConfig: KoinConfiguration
	private lateinit var koin: Koin
	
	override fun getKoin() = koin
	
	override fun onEnable() {
		if (::koinConfig.isInitialized) koin = KoinService.new(this, koinConfig)
	}
	
	override fun onDisable() {
		if (::koinConfig.isInitialized) KoinService.close<MCore>()
		cancel("$name has been disabled.")
	}
}
