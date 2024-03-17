package me.matin.core.function

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.util.logging.Level

class Config(private val plugin: Plugin, config: String) {

    private val configYML = if (config.contains(".yml")) config else "$config.yml"
    private val file = File(plugin.dataFolder, configYML)
    private var customFile = YamlConfiguration.loadConfiguration(file)

    fun setup() {
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                plugin.logger.log(Level.WARNING, "Couldn't create $configYML")
            }
        }
    }

    fun get(): FileConfiguration {
        return YamlConfiguration.loadConfiguration(file)
    }

    fun save() {
        try {
            customFile.save(file)
        } catch (e: IOException) {
            plugin.logger.log(Level.WARNING, "Couldn't create $configYML")
        }
    }

    fun reload() {
        customFile = YamlConfiguration.loadConfiguration(file)
    }
}