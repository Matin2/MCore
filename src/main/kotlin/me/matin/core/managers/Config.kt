package me.matin.core.managers

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories

class Config(plugin: Plugin, config: String) {

    private var conf: String = config
        get() {
            while (field.startsWith('/')) {
                field = field.removePrefix("/")
            }
            if (!field.endsWith(".yml")) return "$field.yml".trim()
            return field.trim()
        }

    private val path = Path("${plugin.dataFolder.path}/${conf}")
    private lateinit var configFile: FileConfiguration

    @Throws(IOException::class)
    fun init() {
        path.normalize()
        path.createParentDirectories()
        if (!path.toFile().exists()) path.createFile()
        configFile = YamlConfiguration.loadConfiguration(path.toFile())
    }

    fun get(): FileConfiguration = configFile

    @Throws(IOException::class)
    fun save() = configFile.save(path.toFile())

    fun reload() {
        configFile = YamlConfiguration.loadConfiguration(path.toFile())
    }
}