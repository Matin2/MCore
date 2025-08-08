package me.matin.mcore

import com.github.retrooper.packetevents.PacketEvents
import com.github.thesilentpro.headdb.api.HeadAPI
import de.tr7zw.changeme.nbtapi.NBT
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build
import me.matin.mcore.managers.InventoryTitle
import me.matin.mcore.managers.hook.Hook
import me.matin.mcore.managers.hook.HooksListener
import me.matin.mcore.managers.hook.HooksManager
import me.matin.mcore.methods.registerListeners
import me.matin.mlib.text
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getScheduler
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.measureTime

class MCore: JavaPlugin() {
	
	override fun onEnable() = measureTime {
		instance = this
		PacketEvents.getAPI().apply {
			init()
			eventManager.registerListeners(InventoryTitle)
		}
		Depends.manage()
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
	}.run { logger.info("Plugin disabled in ${text()}.") }
	
	companion object {
		
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