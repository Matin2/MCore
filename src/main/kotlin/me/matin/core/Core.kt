package me.matin.core

import com.github.retrooper.packetevents.PacketEvents
import de.tr7zw.changeme.nbtapi.NBTContainer
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.arcaniax.hdb.api.HeadDatabaseAPI
import me.matin.core.managers.PacketManager
import me.matin.core.managers.dependency.Dependency
import me.matin.core.managers.dependency.DependencyListener
import me.matin.core.managers.menu.MenuListener
import me.matin.mlib.text
import net.skinsrestorer.api.SkinsRestorer
import net.skinsrestorer.api.SkinsRestorerProvider
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.measureTime

class Core: JavaPlugin() {

    override fun onEnable() = measureTime {
        instance = this
        CommandAPI.onEnable()
        PacketEvents.getAPI().apply {
            init()
            eventManager.registerListeners(packetInvTitle)
        }
        depends = Depends()
        setPlayerTrackingRange(corePlayerTrackingRange)
        server.pluginManager.apply {
            registerEvents(MenuListener, instance)
            registerEvents(DependencyListener, instance)
        }
    }.run { logger.info("Plugin enabled in ${text()}.") }

    override fun onLoad() = measureTime {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI()?.also {
            it.settings
                .reEncodeByDefault(false)
                .checkForUpdates(false)
            it.load()
        }
        CommandAPI.onLoad(
            CommandAPIBukkitConfig(this)
                .shouldHookPaperReload(true)
                .silentLogs(true)
                .usePluginNamespace()
                .initializeNBTAPI(NBTContainer::class.java, ::NBTContainer)
        )
    }.run { logger.info("Plugin loaded in ${text()}.") }

    override fun onDisable() = measureTime {
        Bukkit.getScheduler().cancelTasks(this)
        Bukkit.getOnlinePlayers().forEach(MenuListener::checkCursor)
        CommandAPI.onDisable()
        PacketEvents.getAPI().terminate()
    }.run { logger.info("Plugin disabled in ${text()}.") }

    private fun setPlayerTrackingRange(playerTrackingRange: MutableMap<World, Int>) {
        playerTrackingRange.clear()
        val defaultRange =
            Bukkit.getServer().spigot().config.getInt("world-settings.default.entity-tracking-range.players", 64)
        Bukkit.getServer().worlds.forEach {
            playerTrackingRange[it] = Bukkit.getServer().spigot()
                .config.getInt("world-settings.${it.name}.entity-tracking-range.players", defaultRange)
        }
    }

    internal class Depends {

        var skinsRestorer: SkinsRestorer? = null
        var headDatabase: HeadDatabaseAPI? = null
        var headDB: Boolean = false

        init {
            Dependency("SkinsRestorer").apply {
                if (state.boolean) skinsRestorer = SkinsRestorerProvider.get()
                onStateChange {
                    skinsRestorer = when (it.boolean) {
                        true -> SkinsRestorerProvider.get()
                        false -> null
                    }
                }
            }
            Dependency("HeadDatabase").apply {
                if (state.boolean) headDatabase = HeadDatabaseAPI()
                onStateChange {
                    headDatabase = when (it.boolean) {
                        true -> HeadDatabaseAPI()
                        false -> null
                    }
                }
            }
            Dependency("HeadDB").apply {
                headDB = state.boolean
                onStateChange { headDB = it.boolean }
            }
        }
    }

    companion object {

        @JvmStatic
        lateinit var instance: Core

        @JvmStatic
        internal lateinit var depends: Depends

        @JvmStatic
        var corePlayerTrackingRange: MutableMap<World, Int> = HashMap()
        internal val packetInvTitle = PacketManager.InventoryTitle()
    }
}