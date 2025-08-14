package me.matin.mcore

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import de.tr7zw.changeme.nbtapi.NBT
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build
import kotlinx.coroutines.*
import me.matin.mcore.managers.InventoryTitle
import me.matin.mcore.managers.hook.HooksHandler
import me.matin.mcore.methods.enabled
import me.matin.mcore.methods.registerListeners
import me.matin.mlib.text
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.Executor
import kotlin.time.measureTime

class MCore: JavaPlugin() {
	
	override fun onEnable() = measureTime {
		instance = this
		pluginScope = CoroutineScope(CoroutineName("MCoreScope") + SupervisorJob() + Dispatchers.Default)
		checkNBTAPI()
		packetEvents = PacketEvents.getAPI().apply {
			init()
			eventManager.registerListeners(InventoryTitle)
		}
		registerListeners(HooksHandler)
		Hooks.manager.manageEnable()
	}.run { componentLogger.info("Plugin enabled in ${text()}.") }
	
	@Suppress("UnstableApiUsage")
	override fun onLoad() = measureTime {
		PacketEvents.setAPI(build(this))
		PacketEvents.getAPI().apply {
			settings.reEncodeByDefault(false).checkForUpdates(false)
			load()
		}
	}.run { componentLogger.info("Plugin loaded in ${text()}.") }
	
	override fun onDisable() = measureTime {
		Hooks.manager.manageDisable()
		packetEvents.terminate()
		pluginScope.cancel(CancellationException("Plugin has been disabled."))
	}.run { componentLogger.info("Plugin disabled in ${text()}.") }
	
	private fun checkNBTAPI() {
		if (NBT.preloadApi()) return
		componentLogger.error("NBT-API wasn't initialized properly, disabling the plugin.")
		enabled = false
	}
	
	companion object {
		
		@JvmStatic
		lateinit var pluginScope: CoroutineScope private set
		
		@JvmStatic
		lateinit var packetEvents: PacketEventsAPI<*> private set
		
		@JvmStatic
		lateinit var instance: MCore private set
	}
}

@Suppress("unused")
val serverDispatcher = Executor { it.run() }.asCoroutineDispatcher()