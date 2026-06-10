package me.matin.mcore.managers.hook

import me.matin.mcore.managers.plugin.KotlinPlugin
import me.matin.mcore.methods.enabled
import org.bukkit.Bukkit
import kotlin.properties.ReadOnlyProperty

@Suppress("unused")
class HooksHandler internal constructor(private val plugin: KotlinPlugin) {
	
	val hooks: MutableSet<Hook> = []
	
	init {
		HooksManager.handlers += this
	}
	
	fun handle(name: String, required: Boolean = false, handler: Hook.() -> Unit = {}) {
		hooks += Hook(name, required).apply {
			handler()
			Bukkit.getPluginManager().getPlugin(name)?.let { check(it) }
		}
		checkRequired()
	}
	
	operator fun get(name: String) = hooks.find { it.name == name }?.hooked ?: false
	
	fun bind(name: String) = ReadOnlyProperty<Any?, Boolean> { _, _ ->
		get(name)
	}
	
	fun <T> bind(name: String, binder: () -> T) = ReadOnlyProperty<Any?, T?> { _, _ ->
		if (get(name)) binder() else null
	}
	
	internal fun close() {
		HooksManager.handlers -= this
	}
	
	internal fun checkRequired() {
		plugin.enabled = hooks.any { it.required && !it.hooked }
	}
}
