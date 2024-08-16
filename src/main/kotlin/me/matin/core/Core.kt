package me.matin.core

import com.github.retrooper.packetevents.PacketEvents
import de.tr7zw.changeme.nbtapi.NBTContainer
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.arcaniax.hdb.api.HeadDatabaseAPI
import me.matin.core.managers.Extras.text
import me.matin.core.managers.PacketManager
import me.matin.core.managers.dependency.DependencyListener
import me.matin.core.managers.dependency.PluginManager
import me.matin.core.managers.menu.MenuListener
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
        val softDepends = setOf("SkinsRestorer", "HeadDatabase", "HeadDB")
        depends = Depends(softDepends)
        setPlayerTrackingRange(corePlayerTrackingRange)
        server.pluginManager.apply {
            registerEvents(MenuListener, instance)
            registerEvents(DependencyListener, instance)
            registerEvents(TestListener, instance)
        }
    }.let { logger.info("Plugin enabled in ${it.text()}.") }

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
    }.let { logger.info("Plugin loaded in ${it.text()}.") }

    override fun onDisable() = measureTime {
        Bukkit.getScheduler().cancelTasks(this)
        Bukkit.getOnlinePlayers().forEach(MenuListener::checkCursor)
        CommandAPI.onDisable()
        PacketEvents.getAPI().terminate()
    }.let { logger.info("Plugin disabled in ${it.text()}.") }

    private fun setPlayerTrackingRange(playerTrackingRange: MutableMap<World, Int>) {
        playerTrackingRange.clear()
        val defaultRange =
            Bukkit.getServer().spigot().config.getInt("world-settings.default.entity-tracking-range.players", 64)
        Bukkit.getServer().worlds.forEach {
            playerTrackingRange[it] = Bukkit.getServer().spigot()
                .config.getInt("world-settings.${it.name}.entity-tracking-range.players", defaultRange)
        }
    }

    class Depends(depends: Set<String>) {

        var skinsRestorer: SkinsRestorer? = null
        var headDatabase: HeadDatabaseAPI? = null
        var headDB: Boolean = false

        init {
            checkDepends(depends)
            monitorDepends(depends)
        }

        private fun checkDepends(softDepends: Set<String>) = PluginManager.checkState(softDepends) { installed, _ ->
            if ("SkinsRestorer" in installed) skinsRestorer = SkinsRestorerProvider.get()
            if ("HeadDatabase" in installed) headDatabase = HeadDatabaseAPI()
            if ("HeadDB" in installed) headDB = true
        }

        private fun monitorDepends(softDepends: Set<String>) = PluginManager.monitorState(
            softDepends.toSet()
        ) { installed, missing ->
            when ("SkinsRestorer") {
                in installed -> skinsRestorer = SkinsRestorerProvider.get()
                in missing -> skinsRestorer = null
            }
            when ("HeadDatabase") {
                in installed -> headDatabase = HeadDatabaseAPI()
                in missing -> headDatabase = null
            }
            when ("HeadDB") {
                in installed -> headDB = true
                in missing -> headDB = false
            }
        }
    }

    companion object {

        @JvmStatic
        lateinit var instance: Core

        @JvmStatic
        lateinit var depends: Depends

        @JvmStatic
        var corePlayerTrackingRange: MutableMap<World, Int> = HashMap()
        internal val packetInvTitle = PacketManager.InventoryTitle()
    }
}