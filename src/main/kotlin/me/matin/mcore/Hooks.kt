package me.matin.mcore

import me.matin.mcore.managers.hook.Hook

internal object Hooks {
	
	val skinsRestorer = Hook("SkinsRestorer", false)
	
	fun init() {
		mcore.hooksHandler += skinsRestorer
	}
}