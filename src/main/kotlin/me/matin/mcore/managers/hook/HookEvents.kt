@file:Suppress("unused")

package me.matin.mcore.managers.hook

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class HookStateChangeEvent(val hook: Hook): Event(true) {
	
	override fun getHandlers(): HandlerList = handlerList
	
	companion object {
		
		@JvmStatic
		val handlerList: HandlerList = HandlerList()
	}
}

class HookInitialStateCheckEvent(val hook: Hook): Event(true) {
	
	override fun getHandlers(): HandlerList = handlerList
	
	companion object {
		
		@JvmStatic
		val handlerList: HandlerList = HandlerList()
	}
}