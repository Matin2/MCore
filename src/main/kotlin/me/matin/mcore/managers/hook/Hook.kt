package me.matin.mcore.managers.hook

import org.bukkit.Bukkit.getPluginManager
import org.bukkit.event.Listener
import kotlin.reflect.KProperty

open class Hook(
	internal val name: String,
	internal val required: Boolean,
	internal val versionCheck: (String) -> Boolean = { true },
): Listener {
	
	val plugin get() = getPluginManager().getPlugin(name)
	var available = false
		internal set
	
	open fun onCheck() {}
	open fun onFirstCheck() {}
	open fun extraChecks(): Boolean = true
	
	operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = available
}