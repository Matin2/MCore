package com.github.matin2.mcore.managers.hook

import com.github.matin2.mcore.managers.plugin.KotlinPlugin
import com.github.matin2.mcore.methods.utils.enabled
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

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
		Bukkit.getPluginManager().getPlugin(name)?.let { hook.check(it, true) }
		hooks += hook
		checkRequired()
	}
	
	@Suppress("NOTHING_TO_INLINE")
	internal inline fun check(plugin: Plugin) = hooks.find { it.name == plugin.name }?.check(plugin)
	
	operator fun get(name: String) = hooks.first { it.name.equals(name, true) }
	
	inline fun <reified T : Any> bind(name: String, noinline binder: () -> T) = get(name).BoundValue(binder)
	
	internal fun close() {
		HooksManager.hooksHandlers -= this
	}
	
	internal fun checkRequired() {
		plugin.enabled = hooks.filter { it.required }.all { it.hooked }
	}
}
