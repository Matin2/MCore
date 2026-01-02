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
import kotlin.time.measureTime

lateinit var mcore: MCore private set
inline val dispatchers get() = mcore.dispatchers

class MCore: KotlinPlugin() {
	
	lateinit var packetEventsAPI: PacketEventsAPI<*> private set
	
	override fun onEnable() = measureTime {
		mcore = this
		super.onEnable()
		checkNBTAPI()
		packetEventsAPI.init()
		packetEventsAPI.eventManager.registerListeners(InventoryTitle)
		registerListeners(HooksManager)
		Hooks.init()
		componentLogger.info("Plugin is successfully enabled.")
	}.let { componentLogger.debug("Took $it to enable.") }
	
	@Suppress("UnstableApiUsage")
	override fun onLoad() = measureTime {
		PacketEvents.setAPI(build(this))
		packetEventsAPI = PacketEvents.getAPI().apply {
			settings.reEncodeByDefault(false).checkForUpdates(false)
			load()
		}
	}.let { componentLogger.debug("Plugin is successfully loaded in $it.") }
	
	override fun onDisable() = measureTime {
		super.onDisable()
		packetEventsAPI.terminate()
		componentLogger.info("Plugin is disabled.")
	}.let { componentLogger.debug("Took $it to disable.") }
	
	private fun checkNBTAPI() {
		if (NBT.preloadApi()) return
		componentLogger.error("NBT-API wasn't properly loaded, disabling the plugin.")
		enabled = false
	}
}
