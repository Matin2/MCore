package me.matin.mcore

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import de.tr7zw.changeme.nbtapi.NBT
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build
import kotlinx.coroutines.cancel
import me.matin.mcore.managers.InventoryTitle
import me.matin.mcore.managers.hook.HooksManager
import me.matin.mcore.managers.plugin.KotlinPlugin
import me.matin.mcore.managers.plugin.MainBukkitDispatcher
import me.matin.mcore.managers.plugin.MainBukkitDispatcher.initBukkitDispatcher
import me.matin.mcore.methods.enabled
import org.koin.core.component.inject
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

class MCore : KotlinPlugin() {
	
	private lateinit var packetEventsAPI: PacketEventsAPI<*>
	
	private val hooksManager: HooksManager by inject()
	
	private val hooks: Hooks by inject()
	
	override fun onEnable() {
		initBukkitDispatcher()
		checkNBTAPI()
		packetEventsAPI.init()
		enableKoin(module {
			single { packetEventsAPI }
			single<HooksManager>()
			single<Hooks>()
		})
		packetEventsAPI.eventManager.registerListeners(InventoryTitle)
		hooksManager.init()
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
		MainBukkitDispatcher.cancel()
		packetEventsAPI.terminate()
		componentLogger.info("Plugin got disabled.")
	}
	
	private fun checkNBTAPI() {
		if (NBT.preloadApi()) return
		componentLogger.error("NBT-API wasn't properly loaded, disabling the plugin.")
		enabled = false
	}
}
