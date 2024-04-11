package me.matin.core.managers

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories

class Config(plugin: Plugin, private var config: String) {

    init {
        if (!config.endsWith(".yml")) config = "$config.yml"
        while (config.startsWith('/')) {
            config = config.removePrefix("/")
        }
    }

    private val path = Path("${plugin.dataFolder.path}/$config")
    private lateinit var configFile: FileConfiguration

    @Throws(IOException::class)
    fun init() {
        path.normalize()
        path.createParentDirectories()
        if (!path.toFile().exists()) {
            path.createFile()
        }
        configFile = YamlConfiguration.loadConfiguration(path.toFile())
    }

    fun get(): FileConfiguration {
        return configFile
    }

    @Throws(IOException::class)
    fun save() {
        configFile.save(path.toFile())
    }

    fun reload() {
        configFile = YamlConfiguration.loadConfiguration(path.toFile())
    }
}