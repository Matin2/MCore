package me.matin.mcore.managers.hook

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

@Suppress("unused")
open class Hook(
	val name: String,
	val required: Boolean,
	open val requirements: (Plugin) -> Boolean = { true },
): Listener {
	
	typealias Hooked = Boolean
	
	val plugin get() = instance.plugin
	val isHooked: Hooked get() = instance.stateChanges.value
	val stateChanges: SharedFlow<Hooked> get() = instance.stateChanges.asSharedFlow()
	val initialCheck: Job get() = instance.initialCheck
	internal lateinit var instance: HookInstance
	
	protected open suspend fun onStateChange() {}
	protected open suspend fun onInitialCheck() {}
	
	internal suspend fun init(plugin: Plugin) = coroutineScope {
		launch { instance.stateChanges.collect { onStateChange() } }
		launch {
			instance.initialCheck.join()
			onInitialCheck()
		}
		Bukkit.getPluginManager().registerEvents(this@Hook, plugin)
	}
}