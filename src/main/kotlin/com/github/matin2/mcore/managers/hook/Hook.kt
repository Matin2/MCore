package com.github.matin2.mcore.managers.hook

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.updateAndGet
import org.bukkit.plugin.Plugin
import kotlin.concurrent.Volatile
import kotlin.reflect.KProperty

@Suppress("unused")
class Hook internal constructor(val name: String, val required: Boolean) {
	
	private typealias Condition = (plugin: Plugin) -> Boolean
	
	private val hookState = MutableStateFlow<Boolean?>(null)
	
	val hooked get() = hookState.value ?: false
	
	suspend fun hooked() = hookState.value ?: hookState.filterNotNull().first()
	
	@Volatile
	private var conditionMet: Boolean? = null
	private var condition: Condition = { true }
	
	private var enableMethod = {}
	private var disableMethod = {}
	private var notFoundMethod = {}
	
	operator fun getValue(thisRef: Any?, property: KProperty<*>) = hooked
	
	internal fun check(plugin: Plugin, initial: Boolean = false) {
		val conditionMet = conditionMet ?: condition(plugin).also { conditionMet = it }
		val pluginEnabled = plugin.isEnabled
		val hooked = hookState.updateAndGet { pluginEnabled && conditionMet }!!
		when {
			hooked -> enableMethod()
			initial -> notFoundMethod()
			else -> disableMethod()
		}
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
		
		fun addCondition(block: Condition) {
			val previous = condition
			condition = { previous(it) && block(it) }
			conditionMet = null
		}
	}
	
	inner class BoundValue<Value : Any>(private val binder: () -> Value) {
		
		suspend operator fun invoke(): Value? = if (hooked()) binder() else null
		
		fun current(): Value? = if (hooked) binder() else null
		
		@Suppress("NOTHING_TO_INLINE")
		inline operator fun getValue(thisRef: Any?, property: KProperty<*>) = current()
	}
}
