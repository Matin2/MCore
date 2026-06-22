package com.github.matin2.mcore

import com.github.matin2.mcore.managers.InventoryTitle
import com.github.matin2.mcore.managers.hook.HooksManager
import com.github.matin2.mcore.managers.plugin.BukkitDispatcher
import com.github.matin2.mcore.managers.plugin.BukkitDispatcher.initBukkitDispatcher
import com.github.matin2.mcore.managers.plugin.KotlinPlugin
import com.github.matin2.mcore.methods.enabled
import de.tr7zw.changeme.nbtapi.NBT
import kotlinx.coroutines.cancel
import org.koin.core.component.inject
import org.koin.dsl.module

class MCore : KotlinPlugin() {
	
	private val hooks: Hooks by inject()
	
	override fun onEnable() {
		initBukkitDispatcher()
		checkNBTAPI()
		enableKoin(module { single { Hooks(get()) } })
		hooks.init()
		hooks.packetEvents?.eventManager?.registerListeners(InventoryTitle)
		server.pluginManager.registerEvents(HooksManager, this)
		componentLogger.info("Plugin enabled successfully.")
	}
	
	override fun onDisable() {
		super.onDisable()
		BukkitDispatcher.cancel()
		componentLogger.info("Plugin got disabled.")
	}
	
	private fun checkNBTAPI() {
		if (NBT.preloadApi()) return
		componentLogger.error("NBT-API wasn't properly loaded, disabling the plugin.")
		enabled = false
	}
}
