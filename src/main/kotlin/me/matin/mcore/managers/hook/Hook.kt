package me.matin.mcore.managers.hook

import org.bukkit.Bukkit.getPluginManager
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

open class Hook(
	val name: String,
	val required: Boolean,
	manager: HooksManager,
	private val requirements: (Plugin) -> Boolean = { true },
): Listener {
	
	val plugin
		get() = getPluginManager().getPlugin(name) ?: throw NullPointerException("Plugin $name is not available")
	var available = false
		internal set
	
	init {
		manager.hooks.add(this)
	}
	
	protected open fun requirements() = requirements.invoke(plugin)
	internal fun checkRequirements() = requirements()
}