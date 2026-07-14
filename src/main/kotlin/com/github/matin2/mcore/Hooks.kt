package com.github.matin2.mcore

import com.github.matin2.mcore.methods.utils.component.component
import com.github.retrooper.packetevents.PacketEvents
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.skinsrestorer.api.SkinsRestorerProvider

internal class Hooks(mcore: MCore) {
	
	val skinsRestorer by mcore.hooksHandler.bind(Names.SKINS_RESTORER) { SkinsRestorerProvider.get() }
	val packetEvents by mcore.hooksHandler.bind(Names.PACKET_EVENTS) { PacketEvents.getAPI() }
	
	init {
		val logger = mcore.componentLogger
		val greenColor = Style.style(NamedTextColor.GREEN)
		val redColor = Style.style(NamedTextColor.RED)
		mcore.hooksHandler.handle(Names.SKINS_RESTORER) {
			onEnabled { logger.info(component("Hooked into $name", greenColor)) }
			onDisabled { logger.info(component("UnHooked from $name", redColor)) }
			onNotFound { logger.info(component("Didn't find $name to hook", redColor)) }
		}
		mcore.hooksHandler.handle(Names.PACKET_EVENTS) {
			onEnabled { logger.info(component("Hooked into $name", greenColor)) }
			onDisabled { logger.info(component("UnHooked from $name", redColor)) }
			onNotFound { logger.info(component("Didn't find $name to hook", redColor)) }
		}
	}
	
	private object Names {
		const val SKINS_RESTORER = "SkinsRestorer"
		const val PACKET_EVENTS = "packetevents"
	}
}
