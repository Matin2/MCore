package me.matin.mcore.managers

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Config(plugin: Plugin, config: String) {
	
	private var conf: String = config
		get() = field.run {
			trimStart('/')
			if (!endsWith(".yml")) "$this.yml" else this
		}
	private val configFile = File("${plugin.dataFolder.path}/${conf}")
	lateinit var config: FileConfiguration
	
	operator fun invoke(): FileConfiguration = config
	
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