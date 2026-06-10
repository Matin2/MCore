package me.matin.mcore.managers.hook

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matin.mcore.MCore
import me.matin.mcore.managers.plugin.Bukkit
import me.matin.mcore.managers.plugin.pluginKoin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object HooksManager : KoinComponent {
	
	private val mcore: MCore by inject()
	val handlers: MutableSet<HooksHandler> = []
	private val pluginEvents = callbackFlow {
		val listener = object : Listener {
			@EventHandler
			fun PluginEnableEvent.handle() {
				trySend(plugin to true)
			}
			
			@EventHandler
			fun PluginDisableEvent.handle() {
				trySend(plugin to false)
			}
		}
		mcore.server.pluginManager.registerEvents(listener, mcore)
		awaitClose {
			PluginEnableEvent.getHandlerList().unregister(listener)
			PluginDisableEvent.getHandlerList().unregister(listener)
		}
	}
	
	override fun getKoin() = pluginKoin("MCore")
	
	fun init() {
		mcore.launch(Dispatchers.IO) {
			pluginEvents.buffer().collect { [plugin, onEnable] ->
				this@launch.launch(Dispatchers.Default) {
					val handlers = handlers.filter { handler ->
						handler.hooks.find { it.name == plugin.name }?.check(plugin, onEnable) ?: return@filter false
						true
					}
					if (!onEnable) withContext(Dispatchers.Bukkit) { handlers.forEach(HooksHandler::checkRequired) }
				}
			}
		}
	}
}
