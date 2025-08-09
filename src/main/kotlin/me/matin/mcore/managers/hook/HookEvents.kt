package me.matin.mcore.managers.hook

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

@Suppress("unused")
open class HookCheckEvent(val hook: Hook): Event(true) {
	
	override fun getHandlers(): HandlerList = handlerList
	
	companion object {
		
		val handlerList: HandlerList = HandlerList()
	}
}

class HookInitialCheckEvent(hook: Hook): HookCheckEvent(hook) {
	
	override fun getHandlers(): HandlerList = handlerList
	
	companion object {
		
		val handlerList: HandlerList = HandlerList()
	}
}