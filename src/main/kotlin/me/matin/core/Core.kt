package me.matin.core

import com.github.retrooper.packetevents.PacketEvents
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.matin.core.managers.menu.MenuManager
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class Core: JavaPlugin() {
    companion object {
        @JvmStatic
        var corePlayerTrackingRange: MutableMap<World, Int> = HashMap()
    }

    override fun onEnable() {
        PacketEvents.getAPI().init()
        setPlayerTrackingRange(corePlayerTrackingRange)
        server.pluginManager.registerEvents(MenuManager(), this)
        logger.log(Level.INFO, "Plugin enabled.")
    }

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().settings.reEncodeByDefault(false)
            .checkForUpdates(true)
            .bStats(false)
        PacketEvents.getAPI().load()
        logger.log(Level.INFO, "Plugin loaded.")
    }

    override fun onDisable() {
        PacketEvents.getAPI().terminate()
        logger.log(Level.INFO, "Plugin disabled.")
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