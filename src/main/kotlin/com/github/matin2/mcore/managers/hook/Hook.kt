package com.github.matin2.mcore.managers.hook

import org.bukkit.plugin.Plugin
import kotlin.concurrent.Volatile
import kotlin.reflect.KProperty

@Suppress("unused")
class Hook internal constructor(val name: String, val required: Boolean) {
	
	private typealias Requirement = (plugin: Plugin) -> Boolean
	
	@Volatile
	var hooked = false
		private set
	
	@Volatile
	private var requirementMatched: Boolean? = null
	private var requirement: Requirement = { true }
	
	private var enableMethod = {}
	private var disableMethod = {}
	private var notFoundMethod = {}
	
	operator fun getValue(thisRef: Any?, property: KProperty<*>) = hooked
	
	internal fun check(plugin: Plugin, initial: Boolean = false) {
		val requirementCheck = requirementMatched ?: requirement(plugin).also { requirementMatched = it }
		val hooked = plugin.isEnabled && requirementCheck
		if (hooked == this@Hook.hooked) return
		this@Hook.hooked = hooked
		if (hooked) enableMethod() else if (initial) notFoundMethod() else disableMethod()
	}
	
	inner class Handler {
		
		val name get() = this@Hook.name
		val required get() = this@Hook.required
		
		fun onEnabled(block: () -> Unit) {
			val previous = enableMethod
			enableMethod = {
				previous()
				block()
			}
		}
		
		fun onDisabled(block: () -> Unit) {
			val previous = disableMethod
			disableMethod = {
				previous()
				block()
			}
		}
		
		fun onNotFound(block: () -> Unit) {
			val previous = notFoundMethod
			notFoundMethod = {
				previous()
				block()
			}
		}
		
		fun addRequirement(block: Requirement) {
			val previous = requirement
			requirement = { previous(it) && block(it) }
			requirementMatched = null
		}
	}
}
