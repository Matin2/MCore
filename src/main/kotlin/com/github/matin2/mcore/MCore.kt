package com.github.matin2.mcore

import com.github.matin2.mcore.managers.InventoryTitle
import com.github.matin2.mcore.managers.hook.HooksManager
import com.github.matin2.mcore.managers.plugin.BukkitDispatcher
import com.github.matin2.mcore.managers.plugin.BukkitDispatcher.initBukkitDispatcher
import com.github.matin2.mcore.managers.plugin.KotlinPlugin
import com.github.matin2.mcore.methods.enabled
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import de.tr7zw.changeme.nbtapi.NBT
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build
import kotlinx.coroutines.cancel
import org.koin.core.component.inject
import org.koin.dsl.module

class MCore : KotlinPlugin() {
	
	private val hooks: Hooks by inject()
	
	override fun onEnable() {
		initBukkitDispatcher()
		checkNBTAPI()
		packetEventsAPI!!.init()
		enableKoin(module { single { Hooks(get()) } })
		packetEventsAPI!!.eventManager.registerListeners(InventoryTitle)
		HooksManager.init()
		hooks.init()
		componentLogger.info("Plugin enabled successfully.")
	}
	
	override fun onLoad() {
		PacketEvents.setAPI(build(this))
		packetEventsAPI = PacketEvents.getAPI().apply {
			@Suppress("UnstableApiUsage")
			settings.reEncodeByDefault(false).checkForUpdates(false)
			load()
		}
	}
	
	override fun onDisable() {
		super.onDisable()
		BukkitDispatcher.cancel()
		packetEventsAPI?.terminate()
		packetEventsAPI = null
		componentLogger.info("Plugin got disabled.")
	}
	
	private fun checkNBTAPI() {
		if (NBT.preloadApi()) return
		componentLogger.error("NBT-API wasn't properly loaded, disabling the plugin.")
		enabled = false
	}
	
	companion {
		var packetEventsAPI: PacketEventsAPI<*>? = null
			private set
	}
}
