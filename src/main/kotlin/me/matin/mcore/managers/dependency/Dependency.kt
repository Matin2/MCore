package me.matin.mcore.managers.dependency

import org.bukkit.Bukkit.getPluginManager

class Dependency internal constructor(
	val name: String,
	val required: Boolean,
	internal val versionCheck: (String) -> Boolean,
) {
	
	internal inline val plugin get() = getPluginManager().getPlugin(name)
	var available = false
		internal set
}