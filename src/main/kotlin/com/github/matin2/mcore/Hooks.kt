package com.github.matin2.mcore

import com.github.matin2.mcore.managers.hook.HooksHandler
import net.skinsrestorer.api.SkinsRestorerProvider

internal class Hooks(private val hooksHandler: HooksHandler) {
	
	val skinsRestorer by hooksHandler.bind("SkinsRestorer") { SkinsRestorerProvider.get() }
	
	fun init() = hooksHandler.handle("SkinsRestorer")
}
