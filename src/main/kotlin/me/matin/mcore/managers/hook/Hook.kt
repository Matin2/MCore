package me.matin.mcore.managers.hook

import org.bukkit.plugin.Plugin

@Suppress("unused")
class Hook(val name: String, val required: Boolean) {
	
	typealias Requirement = (Plugin) -> Boolean
	typealias Action = () -> Unit
	
	internal val requirements = mutableSetOf<Requirement>()
	internal var enableAction: Action? = null
	internal var disableAction: Action? = null
	internal var hooked = false
	
	fun onEnable(block: () -> Unit) {
		enableAction = block
	}
	
	fun onDisable(block: () -> Unit) {
		disableAction = block
	}
	
	fun requirement(block: (Plugin) -> Boolean) {
		requirements += block
	}
	
	internal fun check(plugin: Plugin, onEnable: Boolean) {
		hooked = plugin.isEnabled && requirements.all { it(plugin) }
		if (onEnable) enableAction?.invoke() else disableAction?.invoke()
	}
}
