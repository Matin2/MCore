package me.matin.mcore.managers.dependency

import org.bukkit.Bukkit.getPluginManager
import kotlin.reflect.KProperty

class Dependency internal constructor(
	val name: String,
	val required: Boolean,
	val versionCheck: (String) -> Boolean,
) {
	
	inline val plugin get() = getPluginManager().getPlugin(name)
	var available = false
		internal set
	
	@Suppress("unused")
	inner class Available internal constructor() {
		
		val bool get() = available
		
		operator fun getValue(thisRef: Any, property: KProperty<*>) = available
	}
	
	inner class Value<T: Any>(value: T) {
		
		val value: T? = value
			get() = field.takeIf { available }
		
		@Suppress("unused")
		operator fun getValue(thisRef: Any, property: KProperty<*>) = value
	}
}