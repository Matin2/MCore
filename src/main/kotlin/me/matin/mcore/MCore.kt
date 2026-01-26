package me.matin.mcore

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import de.tr7zw.changeme.nbtapi.NBT
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import me.matin.mcore.Hooks.observeHooks
import me.matin.mcore.managers.InventoryTitle
import me.matin.mcore.managers.hook.HooksManager
import me.matin.mcore.managers.plugin.KotlinPlugin
import me.matin.mcore.managers.plugin.MainBukkitDispatcher
import me.matin.mcore.methods.enabled
import org.bukkit.Bukkit

val mcore by KotlinPlugin.get<MCore>()

class MCore : KotlinPlugin() {
	
	lateinit var packetEventsAPI: PacketEventsAPI<*> private set
	internal lateinit var hooksManager: HooksManager private set
	
	override fun onEnable() {
		super.onEnable()
		MainBukkitDispatcher.dispatcher = Bukkit.getScheduler().getMainThreadExecutor(this).asCoroutineDispatcher()
		checkNBTAPI()
		packetEventsAPI.init()
		packetEventsAPI.eventManager.registerListeners(InventoryTitle)
		hooksManager = HooksManager(this)
		hooksHandler.observeHooks()
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
