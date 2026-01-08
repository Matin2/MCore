package me.matin.mcore.managers.hook

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class HookStateFlow internal constructor(
	private val hook: Hook,
	changes: Flow<Boolean>.() -> Flow<Boolean>,
): Flow<Boolean> by hook.stateChanges.filterNotNull().run(changes) {
	
	val value get() = hook.stateChanges.value ?: false
}
