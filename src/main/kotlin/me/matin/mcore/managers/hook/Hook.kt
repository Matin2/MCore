package me.matin.mcore.managers.hook

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
		requirementMatched = null
	}
	
	internal fun check(plugin: Plugin, enabled: Boolean? = null) {
		val requirementCheck = requirementMatched ?: requirement(plugin).also { requirementMatched = it }
		val hooked = enabled ?: plugin.isEnabled && requirementCheck
		if (hooked == this@Hook.hooked) return
		this@Hook.hooked = hooked
		if (hooked) enableMethod() else disableMethod()
	}
}
