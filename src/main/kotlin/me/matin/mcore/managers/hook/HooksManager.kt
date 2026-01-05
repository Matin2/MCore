package me.matin.mcore.managers.hook

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import me.matin.mcore.MCore
import me.matin.mcore.dispatchers
import me.matin.mcore.methods.registerListeners
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import java.util.concurrent.ConcurrentHashMap

@Suppress("NOTHING_TO_INLINE")
internal class HooksManager(private val mcore: MCore) {
	
	val hookInstances: Set<HookInstance>
		field: MutableSet<HookInstance> = ConcurrentHashMap.newKeySet()
	val handlers: MutableSet<HooksHandler> = mutableSetOf()
	private val pluginsEventFlow = callbackFlow {
		val pluginEnableListener = object: Listener {
			@EventHandler
			fun PluginEnableEvent.onPluginEnable() = trySend(plugin).let {}
		}
		val pluginDisableListener = object: Listener {
			@EventHandler
			fun PluginDisableEvent.onPluginEnable() = trySend(plugin).let {}
		}
		mcore.registerListeners(pluginEnableListener, pluginDisableListener)
		awaitClose {
			PluginEnableEvent.getHandlerList().unregister(pluginEnableListener)
			PluginDisableEvent.getHandlerList().unregister(pluginDisableListener)
		}
	}
	
	init {
		pluginsEventFlow
			.mapNotNull { plugin -> hookInstances.find { plugin == it.plugin } }
			.buffer(3)
			.onEach { instance ->
				instance.check(false)
				instance.handlers.forEach { it.checkRequired() }
			}.flowOn(dispatchers.async).launchIn(mcore)
	}
	
	operator fun plusAssign(instance: HookInstance) {
		hookInstances += instance
	}
	
	operator fun minusAssign(instance: HookInstance) {
		hookInstances -= instance
	}
	
	inline operator fun plusAssign(handler: HooksHandler) {
		handlers += handler
	}
	
	inline operator fun minusAssign(handler: HooksHandler) {
		handlers -= handler
		hookInstances.forEach { it -= handler }
	}
}