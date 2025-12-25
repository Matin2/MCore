package me.matin.mcore.managers.hook

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matin.mcore.dispatchers
import me.matin.mcore.mcore
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

@Suppress("NOTHING_TO_INLINE")
internal object HooksManager: Listener, CoroutineScope {
	
	override val coroutineContext =
		mcore.scope.coroutineContext + SupervisorJob(mcore.scope.coroutineContext.job) + dispatchers.async
	
	@JvmStatic
	val hookInstances: Set<HookInstance>
		field: MutableSet<HookInstance> = mutableSetOf()
	
	@JvmStatic
	private val hooksMutex = Mutex()
	
	@JvmStatic
	val handlers: MutableSet<HooksHandler> = mutableSetOf()
	
	@JvmStatic
	suspend operator fun plusAssign(instance: HookInstance) = hooksMutex.withLock { hookInstances += instance }
	
	@JvmStatic
	suspend operator fun minusAssign(instance: HookInstance) = hooksMutex.withLock { hookInstances -= instance }
	
	@JvmStatic
	inline operator fun plusAssign(handler: HooksHandler) {
		handlers += handler
	}
	
	@JvmStatic
	inline operator fun minusAssign(handler: HooksHandler) {
		handlers -= handler
		launch { hooksMutex.withLock { hookInstances.forEach { it -= handler } } }
	}
	
	@JvmStatic
	private fun check(plugin: Plugin) = launch {
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