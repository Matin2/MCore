package me.matin.mcore

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import de.tr7zw.changeme.nbtapi.NBT
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build
import me.matin.mcore.managers.InventoryTitle
import me.matin.mcore.managers.hook.HooksManager
import me.matin.mcore.managers.plugin.KotlinPlugin
import me.matin.mcore.methods.enabled
import me.matin.mcore.methods.registerListeners

lateinit var mcore: MCore private set
inline val dispatchers get() = mcore.dispatchers

class MCore: KotlinPlugin() {
	
	lateinit var packetEventsAPI: PacketEventsAPI<*> private set
	internal lateinit var hooksManager: HooksManager private set
	
	override fun onEnable() {
		mcore = this
		super.onEnable()
		checkNBTAPI()
		packetEventsAPI.init()
		packetEventsAPI.eventManager.registerListeners(InventoryTitle)
		hooksManager = HooksManager(this).also { registerListeners(it) }
		Hooks.init()
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
		packetEventsAPI.terminate()
		componentLogger.info("Plugin got disabled.")
	}
	
	private fun checkNBTAPI() {
		if (NBT.preloadApi()) return
		componentLogger.error("NBT-API wasn't properly loaded, disabling the plugin.")
		enabled = false
	}
}
