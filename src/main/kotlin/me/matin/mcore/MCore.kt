package me.matin.mcore

import com.github.retrooper.packetevents.PacketEvents
import de.tr7zw.changeme.nbtapi.NBT
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build
import me.arcaniax.hdb.api.HeadDatabaseAPI
import me.matin.mcore.managers.InventoryTitle
import me.matin.mcore.managers.dependency.DependencyListener
import me.matin.mcore.managers.dependency.DependencyManager
import me.matin.mcore.methods.registerListeners
import me.matin.mlib.text
import net.skinsrestorer.api.SkinsRestorerProvider.get
import org.bukkit.Bukkit.getScheduler
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.measureTime

class MCore: JavaPlugin() {
	
	override fun onEnable() = measureTime {
		instance = this
		CommandAPI.onEnable()
		PacketEvents.getAPI().apply {
			init()
			eventManager.registerListeners(InventoryTitle)
		}
		Depends.manage()
		registerListeners(DependencyListener)
	}.run { logger.info("Plugin enabled in ${text()}.") }
	
	@Suppress("UnstableApiUsage")
	override fun onLoad() = measureTime {
		PacketEvents.setAPI(build(this))
		PacketEvents.getAPI()?.apply {
			settings.reEncodeByDefault(false).checkForUpdates(false)
			load()
		}
		CommandAPI.onLoad(
			CommandAPIBukkitConfig(this)
				.shouldHookPaperReload(true)
				.silentLogs(true)
				.usePluginNamespace()
				.initializeNBTAPI(ReadWriteNBT::class.java, NBT::wrapNMSTag)
		)
	}.run { logger.info("Plugin loaded in ${text()}.") }
	
	override fun onDisable() = measureTime {
		getScheduler().cancelTasks(this)
		CommandAPI.onDisable()
		PacketEvents.getAPI().terminate()
	}.run { logger.info("Plugin disabled in ${text()}.") }
	
	companion object {
		
		@JvmStatic
		lateinit var instance: MCore private set
	}
}

internal object Depends: DependencyManager(MCore.instance) {
	
	val skinsRestorer by addDependency("SkinsRestorer", false, get())
	val headDatabase by addDependency("HeadDatabase", false, HeadDatabaseAPI())
	val headDB by addDependency("HeadDB", false)
}