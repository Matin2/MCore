package me.matin.core

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import me.matin.core.function.Plugins
import me.matin.core.menu.MenuListeners
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class Core: JavaPlugin() {
    companion object {
        lateinit var plugin: Core
        lateinit var protocolManager: ProtocolManager
        var corePlayerTrackingRange: MutableMap<World, Int> = HashMap()
    }

    override fun onEnable() {
        plugin = this
        if (Plugins().hasPlugin("ProtocolLib")) protocolManager = ProtocolLibrary.getProtocolManager()
        setPlayerTrackingRange(corePlayerTrackingRange)
        server.pluginManager.registerEvents(MenuListeners(), this)
        this.logger.log(Level.INFO, "Plugin enabled.")
    }

    override fun onDisable() {
        this.logger.log(Level.INFO, "Plugin enabled.")
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