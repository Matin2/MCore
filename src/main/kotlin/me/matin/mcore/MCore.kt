package me.matin.mcore

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import de.tr7zw.changeme.nbtapi.NBT
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build
import kotlinx.coroutines.*
import me.matin.mcore.managers.InventoryTitle
import me.matin.mcore.managers.hook.HooksManager
import me.matin.mcore.methods.enabled
import me.matin.mcore.methods.registerListeners
import me.matin.mlib.text
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.measureTime

class MCore: JavaPlugin() {
	
	override fun onEnable() = measureTime {
		init()
		packetEvents.eventManager.registerListeners(InventoryTitle)
		registerListeners(HooksManager)
		Hooks.init()
		componentLogger.info("Plugin is successfully enabled.")
	}.run { componentLogger.debug("Took ${text()} to enable.") }
	
	@Suppress("UnstableApiUsage")
	override fun onLoad() = measureTime {
		PacketEvents.setAPI(build(this))
		PacketEvents.getAPI().apply {
			settings.reEncodeByDefault(false).checkForUpdates(false)
			load()
		}
	}.run { componentLogger.debug("Plugin is successfully loaded in ${text()}.") }
	
	override fun onDisable() = measureTime {
		packetEvents.terminate()
		val cancellationException = CancellationException("Plugin has been disabled.")
		serverDispatcher.cancel(cancellationException)
		pluginScope.cancel(cancellationException)
		componentLogger.info("Plugin is disabled.")
	}.run { componentLogger.debug("Took ${text()} to disable.") }
	
	private fun init() {
		instance = this
		serverDispatcher = Bukkit.getScheduler().getMainThreadExecutor(instance).asCoroutineDispatcher()
		pluginScope = CoroutineScope(CoroutineName("MCore") + SupervisorJob() + Dispatchers.Default)
		packetEvents = PacketEvents.getAPI().apply { init() }
		checkNBTAPI()
	}
	
	private fun checkNBTAPI() {
		if (NBT.preloadApi()) return
		componentLogger.error("NBT-API wasn't properly loaded, disabling the plugin.")
		enabled = false
	}
	
	companion object {
		
		@JvmStatic
		lateinit var pluginScope: CoroutineScope private set
		
		@JvmStatic
		lateinit var packetEvents: PacketEventsAPI<*> private set
		
		@JvmStatic
		lateinit var instance: MCore private set
		
		@JvmStatic
		lateinit var serverDispatcher: CoroutineDispatcher private set
	}
}
