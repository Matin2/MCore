package me.matin.mcore.managers.hook

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matin.mcore.mcore
import org.bukkit.plugin.Plugin

@Suppress("unused")
open class Hook(
	val name: String,
	val required: Boolean,
	open val requirements: (Plugin) -> Boolean = { true },
) {
	
	typealias Hooked = Boolean
	
	val plugin get() = instance.plugin
	val isHooked: Hooked get() = instance.stateChanges.value ?: false
	val stateChanges: Flow<Hooked> get() = _stateChanges
	
	@Volatile
	internal lateinit var instance: HookInstance
	
	@Volatile
	private lateinit var _stateChanges: Flow<Hooked>
	protected open suspend fun onInitialCheck(initialState: Hooked) {}
	
	context(handler: HooksHandler)
	internal suspend fun init() {
		setInstance()
		_stateChanges = instance.stateChanges.asSharedFlow().filterNotNull()
		handler.scope.launch { onInitialCheck(_stateChanges.first()) }
	}
	
	context(handler: HooksHandler)
	private suspend fun setInstance() {
		instance = mcore.hooksManager.hookInstances.find {
			it.name == name && it.requirements == requirements
		}?.also { it += handler } ?: HookInstance(this, handler).also {
			mcore.hooksManager += it
			it.check(true)
		}
	}
}