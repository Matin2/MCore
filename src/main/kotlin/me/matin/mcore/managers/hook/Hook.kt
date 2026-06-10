package me.matin.mcore.managers.hook

import org.bukkit.plugin.Plugin

@Suppress("unused")
class Hook(val name: String, val required: Boolean) {
	
	typealias Requirement = (plugin: Plugin) -> Boolean
	
	internal var requirement: Requirement = { true }
	internal var enableMethod = {}
	internal var disableMethod = {}
	internal var hooked = false
	
	fun onEnable(block: () -> Unit) {
		enableMethod = {
			enableMethod()
			block()
		}
	}
	
	fun onDisable(block: () -> Unit) {
		disableMethod = {
			disableMethod()
			block()
		}
	}
	
	fun requirement(block: Requirement) {
		requirement = { requirement(it) && block(it) }
	}
	
	internal fun check(plugin: Plugin, onEnable: Boolean) {
		hooked = plugin.isEnabled && requirement(plugin)
		if (onEnable) enableMethod() else disableMethod()
	}
}
