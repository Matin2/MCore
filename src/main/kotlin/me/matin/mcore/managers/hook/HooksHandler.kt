package me.matin.mcore.managers.hook

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matin.mcore.MCore.Companion.pluginScope
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

internal object HooksHandler: Listener {
	
	@JvmStatic
	val scope = CoroutineScope(pluginScope.coroutineContext + SupervisorJob(pluginScope.coroutineContext.job))
	
	@JvmStatic
	val hooks: Map<HookInstance, Set<HooksManager>>
		field: MutableMap<HookInstance, Set<HooksManager>> = mutableMapOf()
	
	@JvmStatic
	private val mutex = Mutex()
	
	@JvmStatic
	suspend infix fun HookInstance.addManager(manager: HooksManager) {
		mutex.withLock { this@HooksHandler.hooks[this] = this@HooksHandler.hooks[this]!!.plus(manager) }
	}
	
	@JvmStatic
	suspend fun removeManager(manager: HooksManager) = hooks.forEach { (hook, managers) ->
		mutex.withLock { this@HooksHandler.hooks[hook] = managers - manager }
	}
	
	@JvmStatic
	suspend fun addInstance(instance: HookInstance, manager: HooksManager) {
		mutex.withLock { hooks[instance] = setOf(manager) }
	}
	
	fun init() = scope.launch {
		for ((hook, managers) in hooks) {
			launch {
				hook.check(true)
				managers.forEach { it.onCheck(true) }
			}
		}
	}
	
	@JvmStatic
	private fun check(plugin: Plugin) = scope.launch {
		for ((hook, managers) in hooks) {
			if (hook.plugin != plugin) continue
			launch {
				hook.check(false)
				managers.forEach { it.onCheck(false) }
			}
		}
	}
	
	@EventHandler
	fun PluginEnableEvent.onPluginEnable() = check(plugin).let {}
	
	@EventHandler
	fun PluginDisableEvent.onPluginDisable() = check(plugin).let {}
}