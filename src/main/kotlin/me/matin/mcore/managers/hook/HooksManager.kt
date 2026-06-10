package me.matin.mcore.managers.hook

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import me.matin.mcore.MCore
import me.matin.mcore.managers.plugin.BukkitDispatcher
import me.matin.mcore.methods.registerListeners
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

internal class HooksManager(private val mcore: MCore) {
	
	val handlers: MutableSet<HooksHandler> = mutableSetOf()
	
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
	
	fun init() {
		pluginsEventFlow.buffer(Channel.UNLIMITED).onEach { [plugin, onEnable] ->
			val handlers = handlers.mapNotNull { handler ->
				handler.hooks.find { it.name == plugin.name }?.check(plugin, onEnable) ?: return@mapNotNull null
				handler
			}
			if (!onEnable) withContext(BukkitDispatcher) { handlers.forEach(HooksHandler::checkRequired) }
		}.flowOn(Dispatchers.Default).launchIn(mcore)
	}
}
