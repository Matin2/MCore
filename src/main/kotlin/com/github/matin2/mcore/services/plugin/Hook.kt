package com.github.matin2.mcore.services.plugin

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin
import kotlin.reflect.KProperty

@Suppress("unused")
abstract class Hook(val name: String) {
	
	lateinit var state: StateFlow<Boolean> private set
	inline val hooked get() = state.value
	
	private val bounded: MutableList<Bound<*>> = []
	
	infix fun hookTo(plugin: KotlinPlugin) {
		state = runBlocking { hookStateFlow(plugin) }
	}
	
	protected fun <T> bind(binder: () -> T) = Bound(binder).also { bounded += it }
	
	protected open fun onEnable() {}
	protected open fun onDisable() {}
	protected open fun onNotFound() {}
	protected open fun withCondition(plugin: Plugin): Boolean = true
	
	private suspend fun hookStateFlow(owner: KotlinPlugin) = callbackFlow {
		val listener = object : Listener {
			@EventHandler
			fun PluginEnableEvent.handle() {
				if (plugin.name != name) return
				val available = withCondition(plugin)
				if (available) onEnable()
				trySend(available)
			}
			
			@EventHandler
			fun PluginDisableEvent.handle() {
				if (plugin.name != name) return
				onDisable()
				bounded.forEach { it.reset() }
				trySend(false)
			}
		}
		Bukkit.getPluginManager().registerEvents(listener, owner)
		awaitClose { HandlerList.unregisterAll(listener) }
	}.buffer(onBufferOverflow = DROP_OLDEST).onStart {
		val available = Bukkit.getPluginManager().getPlugin(name)?.run {
			isEnabled && withCondition(this)
		} ?: false
		emit(available)
		if (available) onEnable() else onNotFound()
	}.stateIn(owner)
	
	operator fun getValue(thisRef: Any?, property: KProperty<*>) = hooked
	
	inner class Bound<Value>(private val binder: () -> Value) {
		
		private var value: Value? = null
		
		internal fun reset() {
			value = null
		}
		
		operator fun invoke(): Value? = value ?: (if (hooked) binder() else null).also { value = it }
		
		@Suppress("NOTHING_TO_INLINE")
		inline operator fun getValue(thisRef: Any?, property: KProperty<*>) = invoke()
	}
}
