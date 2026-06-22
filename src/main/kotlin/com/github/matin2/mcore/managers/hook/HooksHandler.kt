package com.github.matin2.mcore.managers.hook

import com.github.matin2.mcore.managers.plugin.KotlinPlugin
import com.github.matin2.mcore.methods.enabled
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.properties.ReadOnlyProperty

@Suppress("unused")
class HooksHandler internal constructor(private val plugin: KotlinPlugin) {
	
	internal val hooks: MutableSet<Hook> = []
	
	init {
		HooksManager.hooksHandlers += this
	}
	
	fun handle(name: String, required: Boolean = false, handler: Hook.() -> Unit = {}) {
		hooks += Hook(name, required).apply {
			handler()
			Bukkit.getPluginManager().getPlugin(name)?.let { check(it) }
		}
		checkRequired()
	}
	
	@Suppress("NOTHING_TO_INLINE")
	internal inline fun check(plugin: Plugin) = hooks.find { it.name == plugin.name }?.check(plugin)
	
	operator fun get(name: String) = hooks.find { it.name == name }?.hooked ?: false
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun bind(name: String) = ReadOnlyProperty<Any?, Boolean> { _, _ ->
		get(name)
	}
	
	inline fun <reified T : Any> bind(name: String, crossinline binder: () -> T) = ReadOnlyProperty<Any?, T?> { _, _ ->
		if (get(name)) binder() else null
	}
	
	internal fun close() {
		HooksManager.hooksHandlers -= this
	}
	
	internal fun checkRequired() {
		plugin.enabled = hooks.filter { it.required }.all { it.hooked }
	}
}
