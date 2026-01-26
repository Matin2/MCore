package me.matin.mcore.managers.hook

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import me.matin.mcore.MCore
import me.matin.mcore.managers.plugin.MainBukkitDispatcher
import me.matin.mcore.methods.registerListeners
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import java.util.concurrent.ConcurrentHashMap

internal class HooksManager(private val mcore: MCore) {
	
	val hooks: MutableSet<Hook> = ConcurrentHashMap.newKeySet()
	private val pluginsEventFlow = callbackFlow {
		val pluginEnableListener = object : Listener {
			@EventHandler
			fun PluginEnableEvent.handle(): Unit = run { trySend(plugin to true) }
		}
		val pluginDisableListener = object : Listener {
			@EventHandler
			fun PluginDisableEvent.handle(): Unit = run { trySend(plugin to false) }
		}
		mcore.registerListeners(pluginEnableListener, pluginDisableListener)
		awaitClose {
			PluginEnableEvent.getHandlerList().unregister(pluginEnableListener)
			PluginDisableEvent.getHandlerList().unregister(pluginDisableListener)
		}
	}
	
	init {
		pluginsEventFlow.buffer(Channel.UNLIMITED).mapNotNull { (plugin, onEnable) ->
			hooks.find { plugin.name == it.name }?.let { it to onEnable }
		}.onEach { (hook, onEnable) ->
			hook.check(onEnable)
			withContext(MainBukkitDispatcher) {
				if (!onEnable) hook.handlers.forEach { it.checkRequired(hook) }
			}
		}.flowOn(Dispatchers.Default).launchIn(mcore.lifecycleScope)
	}
	
	context(handler: HooksHandler)
	operator fun get(
		name: String,
		requirements: Hook.Requirements?,
		onEnable: Hook.StateAction?,
		onDisable: Hook.StateAction?,
	): Hook = hooks.find { it.name == name }?.apply {
		handlers += handler
		onEnable?.let { enableActions += it }
		onDisable?.let { disableActions += it }
	} ?: Hook(name, requirements, handler, onEnable, onDisable).also { hooks += it }
}