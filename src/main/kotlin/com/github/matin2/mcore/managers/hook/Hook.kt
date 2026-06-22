package com.github.matin2.mcore.managers.hook

import org.bukkit.plugin.Plugin
import kotlin.concurrent.Volatile

@Suppress("unused")
class Hook(val name: String, val required: Boolean) {
	
	typealias Requirement = (plugin: Plugin) -> Boolean
	
	@Volatile
	private var requirementMatched: Boolean? = null
	private var requirement: Requirement = { true }
	
	@Volatile
	internal var hooked = false
	private var enableMethod = {}
	private var disableMethod = {}
	
	fun onEnable(block: () -> Unit) {
		val previous = enableMethod
		enableMethod = {
			previous()
			block()
		}
	}
	
	fun onDisable(block: () -> Unit) {
		val previous = disableMethod
		disableMethod = {
			previous()
			block()
		}
	}
	
	fun requirement(block: Requirement) {
		val previous = requirement
		requirement = { previous(it) && block(it) }
		requirementMatched = null
	}
	
	internal fun check(plugin: Plugin) {
		val requirementCheck = requirementMatched ?: requirement(plugin).also { requirementMatched = it }
		val hooked = plugin.isEnabled && requirementCheck
		if (hooked == this@Hook.hooked) return
		this@Hook.hooked = hooked
		if (hooked) enableMethod() else disableMethod()
	}
}
