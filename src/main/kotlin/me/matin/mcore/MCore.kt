package me.matin.mcore

import com.github.retrooper.packetevents.PacketEvents
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build
import kotlinx.coroutines.*
import me.matin.mcore.managers.InventoryTitle
import me.matin.mcore.managers.hook.Hook
import me.matin.mcore.managers.hook.HooksListener
import me.matin.mcore.managers.hook.HooksManager
import me.matin.mcore.methods.registerListeners
import me.matin.mlib.text
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getScheduler
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.Executor
import kotlin.time.measureTime

class MCore: JavaPlugin() {
	
	override fun onEnable() = measureTime {
		instance = this
		PacketEvents.getAPI().apply {
			init()
			eventManager.registerListeners(InventoryTitle)
		}
		pluginScope = CoroutineScope(CoroutineName("MCoreScope"))
		registerListeners(HooksListener)
	}.run { logger.info("Plugin enabled in ${text()}.") }
	
	@Suppress("UnstableApiUsage")
	override fun onLoad() = measureTime {
		PacketEvents.setAPI(build(this))
		PacketEvents.getAPI()?.apply {
			settings.reEncodeByDefault(false).checkForUpdates(false)
			load()
		}
	}.run { logger.info("Plugin loaded in ${text()}.") }
	
	override fun onDisable() = measureTime {
		PacketEvents.getAPI().terminate()
		pluginScope.cancel(CancellationException("Plugin has been disabled."))
	}.run { logger.info("Plugin disabled in ${text()}.") }
	
	companion object {
		
		@JvmStatic
		lateinit var pluginScope: CoroutineScope private set
		
		@JvmStatic
		lateinit var instance: MCore private set
	}
}

internal object Depends: HooksManager(MCore.instance, HeadDB) {
	
	val skinsRestorer by newHook("SkinsRestorer", false)
	val headDatabase by newHook("HeadDatabase", false)
	
	object HeadDB: Hook("HeadDB", false) {
		
		private val rsp get() = Bukkit.getServicesManager().getRegistration(HeadAPI::class.java)
		
		override fun extraChecks() = rsp != null
		
		val api = rsp?.provider
	}
}