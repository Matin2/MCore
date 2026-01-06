package me.matin.mcore

import me.matin.mcore.managers.hook.Hook
import net.skinsrestorer.api.SkinsRestorer
import net.skinsrestorer.api.SkinsRestorerProvider

internal object Hooks {
	
	object SkinsRestorerHook: Hook("SkinsRestorer", false) {
		
		var api: SkinsRestorer? = null
			private set
		
		override suspend fun onInitialCheck(initialState: Hooked) = stateChanges.collect {
			api = if (it) SkinsRestorerProvider.get() else null
		}
	}
	
	fun init() {
		mcore.hooksHandler += SkinsRestorerHook
	}
}