package me.matin.core

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import me.matin.core.function.Plugins
import me.matin.core.menu.MenuListeners
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin

class Core: JavaPlugin() {

    lateinit var plugin: Core
    lateinit var protocolManager: ProtocolManager
    var playerTrackingRange: MutableMap<World, Int> = HashMap()

    override fun onEnable() {
        plugin = this
        if (Plugins().hasPlugin("ProtocolLib")) protocolManager = ProtocolLibrary.getProtocolManager()
        setPlayerTrackingRange(playerTrackingRange)
        server.pluginManager.registerEvents(MenuListeners(), this)
        println("Plugin enabled.")
    }

    override fun onDisable() {
        println("Plugin disabled.")
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