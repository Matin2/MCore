package com.github.matin2.mcore

import com.github.matin2.mcore.services.hook
import com.github.matin2.mcore.services.plugin.Hook
import com.github.matin2.mcore.utils.component.component
import com.github.retrooper.packetevents.PacketEvents
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.skinsrestorer.api.SkinsRestorerProvider
import org.koin.dsl.module

val hooksModule = module {
	hook { PacketEventsHook(get()) }
	hook { SkinsRestorerHook(get()) }
}

private val greenColor = Style.style(NamedTextColor.GREEN)
private val redColor = Style.style(NamedTextColor.RED)

private fun ComponentLogger.logHookEnabled(name: String) = info(component("Hooked into $name", greenColor))
private fun ComponentLogger.logHookDisabled(name: String) = info(component("UnHooked from $name", redColor))
private fun ComponentLogger.logHookNotFound(name: String) = info(component("Didn't find $name to hook", redColor))

class PacketEventsHook(private val mcore: MCore) : Hook("packetevents") {
	
	val api by bind { PacketEvents.getAPI() }
	
	override fun onEnable() = mcore.componentLogger.logHookEnabled(name)
	override fun onDisable() = mcore.componentLogger.logHookDisabled(name)
	override fun onNotFound() = mcore.componentLogger.logHookNotFound(name)
}

class SkinsRestorerHook(private val mcore: MCore) : Hook("packetevents") {
	
	val api by bind { SkinsRestorerProvider.get() }
	
	override fun onEnable() = mcore.componentLogger.logHookEnabled(name)
	override fun onDisable() = mcore.componentLogger.logHookDisabled(name)
	override fun onNotFound() = mcore.componentLogger.logHookNotFound(name)
}
