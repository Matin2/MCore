package com.github.matin2.mcore

import com.github.matin2.mcore.managers.hook.HooksHandler
import com.github.retrooper.packetevents.PacketEvents
import net.skinsrestorer.api.SkinsRestorerProvider

internal class Hooks(private val hooksHandler: HooksHandler) {
	
	val skinsRestorer by hooksHandler.bind("SkinsRestorer") { SkinsRestorerProvider.get() }
	val packetEvents by hooksHandler.bind("packetevents") { PacketEvents.getAPI() }
	
	fun init() {
		hooksHandler.handle("SkinsRestorer")
		hooksHandler.handle("packetevents")
	}
}
