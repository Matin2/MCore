package com.github.matin2.mcore

import com.github.retrooper.packetevents.PacketEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.skinsrestorer.api.SkinsRestorerProvider

internal class Hooks(private val mcore: MCore) {
	
	val skinsRestorer by mcore.hooksHandler.bind(Names.SKINS_RESTORER) { SkinsRestorerProvider.get() }
	val packetEvents by mcore.hooksHandler.bind(Names.PACKET_EVENTS) { PacketEvents.getAPI() }
	
	fun init() {
		mcore.hooksHandler.handle(Names.SKINS_RESTORER) {
			onEnabled {
				mcore.componentLogger.info(Component.text("Hooked into ${Names.SKINS_RESTORER}", NamedTextColor.GREEN))
			}
			onDisabled {
				mcore.componentLogger.info(Component.text("UnHooked from ${Names.SKINS_RESTORER}", NamedTextColor.RED))
			}
			onNotFound {
				mcore.componentLogger.info(
					Component.text("Didn't find ${Names.SKINS_RESTORER} to hook", NamedTextColor.RED)
				)
			}
		}
		mcore.hooksHandler.handle(Names.PACKET_EVENTS) {
			onEnabled {
				mcore.componentLogger.info(Component.text("Hooked into ${Names.PACKET_EVENTS}", NamedTextColor.GREEN))
			}
			onDisabled {
				mcore.componentLogger.info(Component.text("UnHooked from ${Names.PACKET_EVENTS}", NamedTextColor.RED))
			}
			onNotFound {
				mcore.componentLogger.info(
					Component.text("Didn't find ${Names.PACKET_EVENTS} to hook", NamedTextColor.RED)
				)
			}
		}
	}
	
	private object Names {
		const val SKINS_RESTORER = "SkinsRestorer"
		const val PACKET_EVENTS = "packetevents"
	}
}
