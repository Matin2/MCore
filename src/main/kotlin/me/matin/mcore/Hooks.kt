package me.matin.mcore

import net.skinsrestorer.api.SkinsRestorer
import net.skinsrestorer.api.SkinsRestorerProvider

internal object Hooks {
	
	var skinsRestorer: SkinsRestorer? = null
		private set
	
	suspend fun initSkinsRestorer(): Unit = mcore.hooksHandler.observeHook("SkinsRestorer").collect {
		skinsRestorer = if (it) SkinsRestorerProvider.get() else null
	}
}