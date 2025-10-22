package me.matin.mcore

import me.matin.mcore.managers.hook.Hook
import me.matin.mcore.managers.hook.HooksHandler.Companion.hooksHandler

internal object Hooks {
	
	val skinsRestorer = Hook("SkinsRestorer", false)
	
	fun init() {
		MCore.instance.hooksHandler += skinsRestorer
	}
}