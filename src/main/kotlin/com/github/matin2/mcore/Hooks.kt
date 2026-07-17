package com.github.matin2.mcore

import com.github.matin2.mcore.managers.hook.Hook
import com.github.matin2.mcore.methods.utils.component.component
import com.github.retrooper.packetevents.PacketEvents
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.skinsrestorer.api.SkinsRestorerProvider

internal class Hooks(private val mcore: MCore) {
	
	val skinsRestorer by mcore.hooksHandler.bind(SKINS_RESTORER) { SkinsRestorerProvider.get() }
	val packetEvents by mcore.hooksHandler.bind(PACKET_EVENTS) { PacketEvents.getAPI() }
	
	fun init() {
		fun Hook.Handler.log() {
			val logger = mcore.componentLogger
			val greenColor = Style.style(NamedTextColor.GREEN)
			val redColor = Style.style(NamedTextColor.RED)
			onEnabled { logger.info(component("Hooked into $name", greenColor)) }
			onDisabled { logger.info(component("UnHooked from $name", redColor)) }
			onNotFound { logger.info(component("Didn't find $name to hook", redColor)) }
		}
		mcore.hooksHandler.handle(SKINS_RESTORER, handler = Hook.Handler::log)
		mcore.hooksHandler.handle(PACKET_EVENTS, handler = Hook.Handler::log)
	}
	
	companion object {
		const val SKINS_RESTORER = "SkinsRestorer"
		const val PACKET_EVENTS = "packetevents"
	}
}
