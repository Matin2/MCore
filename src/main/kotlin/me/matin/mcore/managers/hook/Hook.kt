package me.matin.mcore.managers.hook

import org.bukkit.Bukkit.getPluginManager
import org.bukkit.event.Listener
import kotlin.reflect.KProperty

open class Hook(
	open val name: String,
	open val required: Boolean,
	private val versionCheck: (String) -> Boolean = { true },
): Listener {
	
	inline val plugin get() = getPluginManager().getPlugin(name)
	var available = false
		internal set
	
	open fun versionCheck(version: String): Boolean = versionCheck.invoke(version)
	
	operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = available
}