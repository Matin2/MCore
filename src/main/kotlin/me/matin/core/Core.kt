package me.matin.core

import com.github.retrooper.packetevents.PacketEvents
import de.tr7zw.changeme.nbtapi.NBTContainer
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.arcaniax.hdb.api.HeadDatabaseAPI
import me.matin.core.managers.TextManager.toReadableString
import me.matin.core.managers.dependency.DependencyListener
import me.matin.core.managers.dependency.PluginManager
import me.matin.core.managers.menu.MenuManager
import net.skinsrestorer.api.SkinsRestorer
import net.skinsrestorer.api.SkinsRestorerProvider
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.measureTime

class Core: JavaPlugin() {

    companion object {

        @JvmStatic
        lateinit var instance: Core

        @JvmStatic
        var skinsRestorer: SkinsRestorer? = null

        @JvmStatic
        var headDatabase: HeadDatabaseAPI? = null

        @JvmStatic
        var headDB = false

        @JvmStatic
        var corePlayerTrackingRange: MutableMap<World, Int> = HashMap()
    }

    override fun onEnable() = measureTime {
        instance = this
        CommandAPI.onEnable()
        PacketEvents.getAPI().init()
        checkDepends()
        monitorDepends()
        setPlayerTrackingRange(corePlayerTrackingRange)
        server.pluginManager.registerEvents(MenuManager, this)
        server.pluginManager.registerEvents(DependencyListener, this)
    }.let { logger.info("Plugin enabled in ${it.toReadableString()}.") }

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
    }.let { logger.info("Plugin loaded in ${it.toReadableString()}.") }

    override fun onDisable() = measureTime {
        Bukkit.getScheduler().activeWorkers.filterNotNull().filter { it.owner == this }.forEach {
            it.thread.interrupt()
        }
        Bukkit.getOnlinePlayers().forEach {
            MenuManager.checkCursor(it)
        }
        CommandAPI.onDisable()
        PacketEvents.getAPI().terminate()
    }.let { logger.info("Plugin disabled in ${it.toReadableString()}.") }

    private fun checkDepends() {
        val depends = setOf("SkinsRestorer", "HeadDatabase", "HeadAPI")
        PluginManager.checkState(depends) { installed, _ ->
            if ("SkinsRestorer" in installed) skinsRestorer = SkinsRestorerProvider.get()
            if ("HeadDatabase" in installed) headDatabase = HeadDatabaseAPI()
            if ("HeadAPI" in installed) headDB = true
        }
    }

    private fun monitorDepends() {
        val depends = setOf("SkinsRestorer", "HeadDatabase", "HeadAPI")
        PluginManager.monitorState(depends) { installed, missing ->
            when ("SkinsRestorer") {
                in installed -> skinsRestorer = SkinsRestorerProvider.get()
                in missing -> skinsRestorer = null
            }
            when ("HeadDatabase") {
                in installed -> headDatabase = HeadDatabaseAPI()
                in missing -> headDatabase = null
            }
            when ("HeadAPI") {
                in installed -> headDB = true
                in missing -> headDB = false
            }
        }
    }

    private fun setPlayerTrackingRange(playerTrackingRange: MutableMap<World, Int>) {
        playerTrackingRange.clear()
        val defaultRange =
            Bukkit.getServer().spigot().config.getInt("world-settings.default.entity-tracking-range.players", 64)
        Bukkit.getServer().worlds.forEach {
            playerTrackingRange[it] = Bukkit.getServer().spigot()
                .config.getInt("world-settings.${it.name}.entity-tracking-range.players", defaultRange)
        }
    }
}