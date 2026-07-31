package com.github.matin2.mcore

import com.github.matin2.mcore.services.PacketService
import com.github.matin2.mcore.services.dialog.DialogService
import com.github.matin2.mcore.services.plugin.BukkitDispatcher
import com.github.matin2.mcore.services.plugin.BukkitDispatcher.initBukkitDispatcher
import com.github.matin2.mcore.services.plugin.KotlinPlugin
import com.github.matin2.mcore.services.search.SearchMenuService
import kotlinx.coroutines.cancel
import org.koin.core.component.get
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

class MCore : KotlinPlugin() {
	
	val module = module {
		single<SearchMenuService>()
		includes(DialogService.module, hooksModule)
	}
	
	override var koinConfig = koinConfiguration {
		modules(module)
	}
	
	override fun onEnable() {
		initBukkitDispatcher()
		super.onEnable()
		get<PacketEventsHook>().api?.eventManager?.registerListeners(PacketService.InventoryTitle)
		componentLogger.info("Plugin enabled successfully.")
	}
	
	override fun onDisable() {
		super.onDisable()
		BukkitDispatcher.cancel()
		componentLogger.info("Plugin got disabled.")
	}
}
