package com.github.matin2.mcore.managers.hook

import com.github.matin2.mcore.managers.plugin.KotlinPlugin
import com.github.matin2.mcore.methods.enabled
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.properties.ReadOnlyProperty

@Suppress("unused")
class HooksHandler internal constructor(private val plugin: KotlinPlugin) {
	
	private typealias HookHandler = Hook.Handler.() -> Unit
	
	internal val hooks: MutableSet<Hook> = []
	
	init {
		HooksManager.hooksHandlers += this
	}
	
	fun handle(name: String, required: Boolean = false, handler: HookHandler = {}) {
		val hook = Hook(name, required)
		hook.Handler().handler()
		Bukkit.getPluginManager().getPlugin(name)?.let { plugin ->
			hook.check(plugin)
		}
		hooks += hook
		checkRequired()
	}
	
	@Suppress("NOTHING_TO_INLINE")
	internal inline fun check(plugin: Plugin) = hooks.find { it.name == plugin.name }?.check(plugin)
	
	operator fun get(name: String) = hooks.first { it.name.equals(name, true) }
	
	inline fun <reified T : Any> bind(name: String, crossinline binder: () -> T): ReadOnlyProperty<Any?, T?> {
		val hook = get(name)
		return ReadOnlyProperty { _, _ -> if (hook.hooked) binder() else null }
	}
	
	internal fun close() {
		HooksManager.hooksHandlers -= this
	}
	
	internal fun checkRequired() {
		plugin.enabled = hooks.filter { it.required }.all { it.hooked }
	}
}
