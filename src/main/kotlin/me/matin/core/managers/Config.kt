package me.matin.core.managers

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Config(plugin: Plugin, config: String) {

    private var conf: String = config
        get() {
            if (field.startsWith('/'))field.trimStart('/')
            if (!field.endsWith(".yml")) return "$field.yml"
            return field
        }

    private val configFile = File("${plugin.dataFolder.path}/${conf}")
    lateinit var config: FileConfiguration

    @Throws(IOException::class)
    fun init() {
        if (!configFile.parentFile.exists()) configFile.parentFile.mkdirs()
        if (!configFile.exists()) configFile.createNewFile()
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    @Throws(IOException::class)
    fun save() = config.save(configFile)

    fun reload() {
        config = YamlConfiguration.loadConfiguration(configFile)
    }
}