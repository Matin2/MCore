package me.matin.mcore.managers.hook

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matin.mcore.dispatchers
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

@Suppress("NOTHING_TO_INLINE")
internal class HooksManager(private val scope: CoroutineScope): Listener {
	
	val hookInstances: Set<HookInstance>
		field: MutableSet<HookInstance> = mutableSetOf()
	private val hooksMutex = Mutex()
	val handlers: MutableSet<HooksHandler> = mutableSetOf()
	
	suspend operator fun plusAssign(instance: HookInstance) = hooksMutex.withLock { hookInstances += instance }
	
	suspend operator fun minusAssign(instance: HookInstance) = hooksMutex.withLock { hookInstances -= instance }
	
	inline operator fun plusAssign(handler: HooksHandler) {
		handlers += handler
	}
	
	inline operator fun minusAssign(handler: HooksHandler) {
		handlers -= handler
		scope.launch(dispatchers.async) { hooksMutex.withLock { hookInstances.forEach { it -= handler } } }
	}
	
	private fun check(plugin: Plugin) = scope.launch(dispatchers.async) {
		hookInstances.find { it.plugin == plugin }?.apply {
			check(false)
			handlers.forEach { launch { it.checkRequired() } }
		}
	}.let {}
	
	@EventHandler
	fun PluginEnableEvent.onPluginEnable() = check(plugin)
	
	@EventHandler
	fun PluginDisableEvent.onPluginDisable() = check(plugin)
}