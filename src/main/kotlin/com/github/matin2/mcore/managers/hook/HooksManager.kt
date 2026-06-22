package com.github.matin2.mcore.managers.hook

import com.github.matin2.mcore.MCore
import com.github.matin2.mcore.managers.plugin.Bukkit
import com.github.matin2.mcore.managers.plugin.KotlinPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object HooksManager : KoinComponent, Listener {
	
	private val mcore: MCore by inject()
	val hooksHandlers: MutableSet<HooksHandler> = []
	
	override fun getKoin() = KotlinPlugin.getKoin("MCore")
	
	@EventHandler
	fun PluginEnableEvent.handle() {
		mcore.launch { hooksHandlers.forEach { it.check(plugin) } }
	}
	
	@EventHandler
	fun PluginDisableEvent.handle() {
		mcore.launch {
			val handlers = hooksHandlers.filter { it.check(plugin) != null }
			withContext(Dispatchers.Bukkit) { handlers.forEach(HooksHandler::checkRequired) }
		}
	}
}
