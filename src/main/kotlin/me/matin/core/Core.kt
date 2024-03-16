package me.matin.core

import me.matin.core.menu.MenuListeners
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Core: JavaPlugin() {

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(MenuListeners(), this)
        println("Plugin enabled.")
    }

    override fun onDisable() {
        println("Plugin disabled.")
    }

}