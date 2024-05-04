package me.matin.core

import com.github.retrooper.packetevents.PacketEvents
import de.tr7zw.changeme.nbtapi.NBTContainer
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.matin.core.managers.menu.MenuManager
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin

class Core: JavaPlugin() {
    companion object {
        @JvmStatic
        var corePlayerTrackingRange: MutableMap<World, Int> = HashMap()
    }

    override fun onEnable() {
        PacketEvents.getAPI().init()
        CommandAPI.onEnable()
        setPlayerTrackingRange(corePlayerTrackingRange)
        server.pluginManager.registerEvents(MenuManager(), this)
        logger.info("Plugin enabled.")
    }

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().settings.reEncodeByDefault(false)
            .checkForUpdates(true)
            .bStats(false)
        PacketEvents.getAPI().load()
        CommandAPI.onLoad(CommandAPIBukkitConfig(this)
            .shouldHookPaperReload(true)
            .silentLogs(true)
            .usePluginNamespace()
            .initializeNBTAPI(NBTContainer::class.java, ::NBTContainer)
        )
        logger.info("Plugin loaded.")
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().forEach {
            MenuManager.checkCursor(it)
        }
        CommandAPI.onDisable()
        PacketEvents.getAPI().terminate()
        logger.info("Plugin disabled.")
    }

    private fun setPlayerTrackingRange(playerTrackingRange: MutableMap<World, Int>) {
        playerTrackingRange.clear()
        val defaultRange =
            Bukkit.getServer().spigot().config.getInt("world-settings.default.entity-tracking-range.players", 64)
        for (world in Bukkit.getServer().worlds) {
            val range = Bukkit.getServer()
                .spigot().config.getInt("world-settings." + world.name + ".entity-tracking-range.players", defaultRange)
            playerTrackingRange[world] = range
        }
    }

}